package com.oam.expense.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oam.expense.dto.InvoiceOcrResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 发票 OCR 识别：对接百度增值税发票识别（vat_invoice）。
 * 未配置 API Key / Secret Key 时前端降级为手工录入。
 */
@Service
public class InvoiceOcrService {

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String INVOICE_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";
    private static final long MAX_FILE_SIZE = 4L * 1024 * 1024;
    private static final DateTimeFormatter DATE_OUT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_IN = DateTimeFormatter.ofPattern("yyyy-M-d");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${oam.invoice-ocr.api-key:}")
    private String apiKey;

    @Value("${oam.invoice-ocr.secret-key:}")
    private String secretKey;

    private String cachedToken;
    private long tokenExpireAt;

    public boolean isEnabled() {
        return notBlank(apiKey) && notBlank(secretKey);
    }

    public String provider() {
        return isEnabled() ? "百度增值税发票识别" : "未配置";
    }

    public InvoiceOcrResult recognize(MultipartFile file) {
        if (!isEnabled()) {
            throw new IllegalArgumentException("未配置发票识别服务，请手工录入发票信息");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传发票图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("发票图片不能超过 4MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("仅支持上传发票图片（jpg / png）");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("读取发票图片失败");
        }
        String base64 = Base64.getEncoder().encodeToString(bytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("image", base64);
        String url = INVOICE_URL + "?access_token=" + accessToken();
        String body;
        try {
            body = restTemplate.postForObject(url, new HttpEntity<>(form, headers), String.class);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("发票识别服务调用失败：" + e.getMessage());
        }
        return parse(body);
    }

    // ==================== 鉴权 ====================

    private String accessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedToken;
        }
        String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey
                + "&client_secret=" + secretKey;
        try {
            String body = restTemplate.postForObject(url, null, String.class);
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("access_token")) {
                cachedToken = node.get("access_token").asText();
                long expiresIn = node.path("expires_in").asLong(2592000);
                // 提前 5 分钟过期，避免边界失效
                tokenExpireAt = System.currentTimeMillis() + Math.max(expiresIn - 300, 60) * 1000;
                return cachedToken;
            }
        } catch (Exception ignored) {
            // 统一抛出下面的提示
        }
        throw new IllegalArgumentException("发票识别服务鉴权失败，请检查 API Key / Secret Key 配置");
    }

    // ==================== 结果解析 ====================

    private InvoiceOcrResult parse(String body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body == null ? "{}" : body);
        } catch (IOException e) {
            throw new IllegalArgumentException("发票识别结果解析失败");
        }
        if (root.has("error_code")) {
            throw new IllegalArgumentException("发票识别失败：" + root.path("error_msg").asText("未知错误"));
        }
        JsonNode words = root.path("words_result");
        if (words.isMissingNode() || words.isNull() || words.isEmpty()) {
            throw new IllegalArgumentException("未识别到发票信息，请上传清晰的发票图片");
        }

        InvoiceOcrResult result = new InvoiceOcrResult();
        result.setInvoiceNumber(text(words, "InvoiceNum"));
        result.setInvoiceCode(text(words, "InvoiceCode"));
        result.setInvoiceDate(normalizeDate(text(words, "InvoiceDate")));
        result.setSellerName(text(words, "SellerName"));
        result.setBuyerName(text(words, "PurchaserName"));
        result.setAmount(amount(words, "AmountInFiguers"));
        result.setTaxAmount(amount(words, "TotalTax"));
        result.setTotalAmount(amount(words, "TotalAmount"));
        result.setInvoiceType(normalizeType(text(words, "InvoiceType")));
        return result;
    }

    /** 兼容 words_result 为对象（字段直接是文本或含 words）或数组两种结构 */
    private String text(JsonNode words, String key) {
        if (words.isArray()) {
            for (JsonNode item : words) {
                JsonNode value = item.get(key);
                if (value != null && !value.isNull()) {
                    return clean(value.isObject() ? value.path("words").asText(null) : value.asText());
                }
            }
            return null;
        }
        JsonNode node = words.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        return clean(node.isObject() ? node.path("words").asText(null) : node.asText());
    }

    private BigDecimal amount(JsonNode words, String key) {
        String value = text(words, key);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").replace("￥", "").replace("¥", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** 百度返回 2019年01月01日 / 2019-01-01 等格式，统一为 yyyy-MM-dd */
    private String normalizeDate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replace("年", "-").replace("月", "-").replace("日", "")
                .replace("/", "-").replace(".", "-").trim();
        if (s.endsWith("-")) {
            s = s.substring(0, s.length() - 1);
        }
        try {
            return LocalDate.parse(s, DATE_IN).format(DATE_OUT);
        } catch (Exception e) {
            return raw;
        }
    }

    private String normalizeType(String raw) {
        if (raw == null) {
            return "OTHER";
        }
        if (raw.contains("电子")) {
            return "VAT_ELECTRONIC";
        }
        if (raw.contains("专用")) {
            return "VAT_SPECIAL";
        }
        if (raw.contains("普通")) {
            return "VAT_NORMAL";
        }
        return "OTHER";
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
