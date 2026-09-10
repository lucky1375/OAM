package com.oam.common.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 简易 Excel(.xlsx) 导出工具。
 */
public final class ExcelUtil {

    private ExcelUtil() {
    }

    /**
     * @param sheetName 工作表名
     * @param header    表头
     * @param rows      数据行
     * @return xlsx 文件字节
     */
    public static byte[] toXlsxBytes(String sheetName, String[] header, List<String[]> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (String[] row : rows) {
                Row dataRow = sheet.createRow(rowIdx++);
                for (int c = 0; c < row.length; c++) {
                    dataRow.createCell(c).setCellValue(row[c] == null ? "" : row[c]);
                }
            }

            for (int i = 0; i < header.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("生成 Excel 失败", e);
        }
    }
}
