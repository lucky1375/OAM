package com.oam.expense.service;

import com.oam.expense.dto.InvoiceOcrResult;
import com.sun.jna.NativeLibrary;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 发票 OCR 识别：基于本地 Tesseract 引擎 + Tess4J 封装。
 * 需安装 Tesseract 及 chi_sim / eng 语言数据，路径通过 oam.invoice-ocr 配置。
 */
@Service
public class InvoiceOcrService {

    private static final long MAX_IMAGE_SIZE = 4L * 1024 * 1024;
    private static final long MAX_PDF_SIZE = 10L * 1024 * 1024;
    /** 扫描版 PDF 渲染识别用的清晰度 */
    private static final int PDF_RENDER_DPI = 300;
    private static final DateTimeFormatter DATE_OUT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_IN = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final Pattern MONEY = Pattern.compile("[¥￥]?\\s*([0-9][0-9,]*\\.[0-9]{2})");

    private final TesseractHolder holder = new TesseractHolder();

    @Value("${oam.invoice-ocr.library-path:}")
    private String libraryPath;

    @Value("${oam.invoice-ocr.tessdata-path:}")
    private String tessdataPath;

    @Value("${oam.invoice-ocr.languages:chi_sim+eng}")
    private String languages;

    @PostConstruct
    public void init() {
        if (notBlank(libraryPath)) {
            // 让 JNA 优先从指定目录加载 libtesseract，再加载其同级依赖
            System.setProperty("jna.library.path", libraryPath);
            try {
                NativeLibrary.addSearchPath("tesseract", libraryPath);
            } catch (Exception ignored) {
                // 目录不存在等场景由 isEnabled() 与识别时的错误提示兜底
            }
        }
    }

    public boolean isEnabled() {
        return tessdataReady() && nativeReady();
    }

    public String provider() {
        return isEnabled() ? "Tesseract OCR（本地）" : "未检测到 Tesseract 环境";
    }

    public InvoiceOcrResult recognize(MultipartFile file) {
        if (!isEnabled()) {
            throw new IllegalArgumentException("未检测到本地 Tesseract OCR 环境（缺少动态库或语言数据），请参考 README 安装后重试");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传发票图片或 PDF 文件");
        }
        if (isPdf(file)) {
            if (file.getSize() > MAX_PDF_SIZE) {
                throw new IllegalArgumentException("发票 PDF 不能超过 10MB");
            }
            return recognizePdf(readBytes(file));
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("发票图片不能超过 4MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("仅支持上传发票图片（jpg / png）或 PDF 文件");
        }
        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("读取发票图片失败");
        }
        if (image == null) {
            throw new IllegalArgumentException("无法解析图片，请上传 jpg / png 格式的发票图片");
        }

        String text = doOcr(image);
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("未识别到发票文字，请上传清晰的发票图片");
        }
        return parse(text);
    }

    /** PDF 发票：文本型直接提取文字，扫描/图片型渲染首页后 OCR */
    private InvoiceOcrResult recognizePdf(byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF 中没有可识别的页面");
            }
            String text = new PDFTextStripper().getText(document);
            if (text != null && !text.trim().isEmpty()) {
                try {
                    return parse(text);
                } catch (IllegalArgumentException ignored) {
                    // 文本层缺少关键字段，继续走渲染识别
                }
            }
            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, PDF_RENDER_DPI);
            String ocrText = doOcr(image);
            if (ocrText == null || ocrText.trim().isEmpty()) {
                throw new IllegalArgumentException("未识别到发票文字，请上传清晰的发票 PDF");
            }
            return parse(ocrText);
        } catch (IOException e) {
            throw new IllegalArgumentException("PDF 解析失败，请确认文件未加密且格式正确");
        }
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("pdf")) {
            return true;
        }
        String name = file.getOriginalFilename();
        return name != null && name.toLowerCase().endsWith(".pdf");
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("读取发票 PDF 失败");
        }
    }

    // ==================== 引擎调用 ====================

    private String doOcr(BufferedImage image) {
        Tesseract tesseract = holder.get();
        if (notBlank(tessdataPath)) {
            tesseract.setDatapath(tessdataPath);
        }
        tesseract.setLanguage(languages);
        // 发票版式规整，按单块识别效果更稳定
        tesseract.setPageSegMode(6);
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("user_defined_dpi", "300");
        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new IllegalArgumentException("发票识别失败：" + e.getMessage());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            throw new IllegalArgumentException("本地 Tesseract 动态库加载失败，请检查 oam.invoice-ocr.library-path 配置");
        }
    }

    /** Tesseract 实例不是线程安全的，按线程复用 */
    private static class TesseractHolder {
        private final ThreadLocal<Tesseract> local = ThreadLocal.withInitial(Tesseract::new);

        Tesseract get() {
            return local.get();
        }
    }

    // ==================== 票面字段解析 ====================

    private InvoiceOcrResult parse(String raw) {
        String text = compact(raw);
        InvoiceOcrResult result = new InvoiceOcrResult();

        result.setInvoiceCode(matchGroup(text, "发票代码\\s*[:：]?\\s*([0-9]{10,12})"));
        result.setInvoiceNumber(firstNonNull(
                matchGroup(text, "发票号码\\s*[:：]?\\s*([0-9]{8,20})"),
                matchGroup(text, "(?:No|NO|№)\\s*[.:：]?\\s*([0-9]{8,20})")));
        result.setInvoiceDate(normalizeDate(matchGroup(text,
                "开票日期\\s*[:：]?\\s*((?:19|20)\\d{2}\\s*[年\\-/.]\\s*\\d{1,2}\\s*[月\\-/.]\\s*\\d{1,2}\\s*日?)")));
        result.setBuyerName(partyName(text, "购买方"));
        result.setSellerName(partyName(text, "销售方"));
        result.setInvoiceType(normalizeType(raw));

        fillAmounts(result, text);

        if (result.getInvoiceNumber() == null && result.getInvoiceCode() == null
                && result.getTotalAmount() == null) {
            throw new IllegalArgumentException("未能从图片中识别出发票关键信息，请手工录入或更换更清晰的图片");
        }
        return result;
    }

    private void fillAmounts(InvoiceOcrResult result, String text) {
        BigDecimal total = amountAfter(text, "价税合计", "小写");
        BigDecimal tax = amountAfter(text, "税额");
        BigDecimal amount = amountAfter(text, "金额");

        // 发票“合计”行通常并排打印：金额 税额
        if (amount == null) {
            List<BigDecimal> lineValues = moneyValuesInLine(text, "合计");
            if (lineValues.size() >= 2) {
                amount = lineValues.get(0);
                if (tax == null) {
                    tax = lineValues.get(1);
                }
            }
        }
        if (total == null) {
            total = maxMoney(text);
        }
        if (amount == null && total != null && tax != null) {
            amount = total.subtract(tax);
        }
        if (tax == null && total != null && amount != null) {
            tax = total.subtract(amount);
        }
        if (total == null && amount != null && tax != null) {
            total = amount.add(tax);
        }

        result.setAmount(scale(amount));
        result.setTaxAmount(scale(tax));
        result.setTotalAmount(scale(total));
    }

    private BigDecimal amountAfter(String text, String... keywords) {
        for (String keyword : keywords) {
            Matcher m = Pattern.compile(keyword + "[^0-9¥￥]{0,30}[¥￥]?\\s*([0-9][0-9,]*\\.[0-9]{2})")
                    .matcher(text);
            if (m.find()) {
                return toAmount(m.group(1));
            }
        }
        return null;
    }

    private List<BigDecimal> moneyValuesInLine(String text, String keyword) {
        List<BigDecimal> values = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            if (!line.contains(keyword) || line.contains("价税合计")) {
                continue;
            }
            Matcher m = MONEY.matcher(line);
            while (m.find()) {
                values.add(toAmount(m.group(1)));
            }
            if (!values.isEmpty()) {
                return values;
            }
        }
        return values;
    }

    private BigDecimal maxMoney(String text) {
        BigDecimal max = null;
        Matcher m = MONEY.matcher(text);
        while (m.find()) {
            BigDecimal value = toAmount(m.group(1));
            if (value != null && (max == null || value.compareTo(max) > 0)) {
                max = value;
            }
        }
        return max;
    }

    private String partyName(String text, String keyword) {
        Matcher m = Pattern.compile(keyword + "[\\s\\S]{0,40}?名称\\s*[:：]\\s*([^\\n]{2,50})").matcher(text);
        if (m.find()) {
            return cleanParty(m.group(1));
        }
        m = Pattern.compile(keyword + "[^\\n]*\\n+\\s*(?:名称\\s*[:：]\\s*)?([^\\n]{2,50})").matcher(text);
        if (m.find()) {
            return cleanParty(m.group(1));
        }
        return null;
    }

    private String cleanParty(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        for (String cut : new String[]{"纳税人", "统一社会信用代码", "地址", "电话", "开户行"}) {
            int idx = v.indexOf(cut);
            if (idx > 0) {
                v = v.substring(0, idx).trim();
            }
        }
        return v.isEmpty() ? null : v;
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

    private String normalizeDate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.replace("年", "-").replace("月", "-").replace("日", "")
                .replace("/", "-").replace(".", "-").replaceAll("\\s+", "").trim();
        if (s.endsWith("-")) {
            s = s.substring(0, s.length() - 1);
        }
        s = s.replaceAll("-+", "-");
        try {
            return LocalDate.parse(s, DATE_IN).format(DATE_OUT);
        } catch (Exception e) {
            return raw.trim();
        }
    }

    // ==================== 文本与数值工具 ====================

    /** 去除 OCR 在汉字之间、数字之间插入的空白，便于关键字匹配 */
    private String compact(String raw) {
        return raw.replaceAll("(?<=[\\u4e00-\\u9fa5])[ \\t]+(?=[\\u4e00-\\u9fa5])", "")
                .replaceAll("(?<=\\d)[ \\t]+(?=\\d)", "");
    }

    private String matchGroup(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        if (m.find()) {
            String value = m.group(1).trim();
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private BigDecimal toAmount(String value) {
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private boolean tessdataReady() {
        if (!notBlank(tessdataPath)) {
            return false;
        }
        File dir = new File(tessdataPath);
        if (!dir.isDirectory()) {
            return false;
        }
        for (String lang : languages.split("\\+")) {
            if (!new File(dir, lang.trim() + ".traineddata").isFile()) {
                return false;
            }
        }
        return true;
    }

    private boolean nativeReady() {
        if (!notBlank(libraryPath)) {
            // 交由系统动态库搜索路径，识别阶段若缺失会给出明确提示
            return true;
        }
        File dir = new File(libraryPath);
        return new File(dir, "libtesseract.dylib").isFile()
                || new File(dir, "libtesseract.so").isFile()
                || new File(dir, "libtesseract.5.dylib").isFile()
                || new File(dir, "tesseract.dll").isFile();
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
