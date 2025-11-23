package com.example.api.account.service.impl;

import com.example.api.account.domain.dto.StatementData;
import com.example.api.account.infrastructure.config.StatementConfigService;
import com.example.api.account.infrastructure.util.StatementColorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatementExcelGenerationServiceImpl implements com.example.api.account.service.StatementExcelGenerationService {
    
    private final StatementConfigService configService;
    private final StatementColorUtil colorUtil;
    private final ResourceLoader resourceLoader;
    
    @Value("${statement.excel.rows.per.page:1000}")
    private int rowsPerPage;
    
    @Value("${statement.excel.header.image.path:images/DukhanBank_Statement_Header.jpg}")
    private String headerImagePath;
    
    @Value("${statement.excel.footer.image.path:images/DukhanBank_Statement_Footer.jpg}")
    private String footerImagePath;
    
    @Value("${statement.excel.header.image.private.path:images/Private_DukhanBank_Statement_Header.jpg}")
    private String privateHeaderImagePath;
    
    @Value("${statement.excel.footer.image.private.path:images/Private_DukhanBank_Statement_Footer.jpg}")
    private String privateFooterImagePath;
    
    public StatementExcelGenerationServiceImpl(StatementConfigService configService, StatementColorUtil colorUtil, ResourceLoader resourceLoader) {
        this.configService = configService;
        this.colorUtil = colorUtil;
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public byte[] generateExcel(String segmentName, StatementData statementData) throws IOException {
        log.info("Starting Excel generation for segment: {}", segmentName);
        XSSFWorkbook workbook = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            workbook = new XSSFWorkbook();
            List<Map<String, Object>> headerFields;
            List<Map<String, Object>> tableColumns;
            
            try {
                headerFields = configService.getHeaderFields(segmentName);
                log.info("Loaded {} header fields for segment: {}", 
                        headerFields != null ? headerFields.size() : 0, segmentName);
            } catch (Exception e) {
                log.error("Error loading header fields for segment {}: {}", segmentName, e.getMessage(), e);
                throw new IOException("Failed to load header configuration: " + e.getMessage(), e);
            }
            
            try {
                tableColumns = configService.getTableColumns(segmentName);
                log.info("Loaded {} table columns for segment: {}", 
                        tableColumns != null ? tableColumns.size() : 0, segmentName);
            } catch (Exception e) {
                log.error("Error loading table columns for segment {}: {}", segmentName, e.getMessage(), e);
                throw new IOException("Failed to load table configuration: " + e.getMessage(), e);
            }
            
            if (headerFields == null || headerFields.isEmpty()) {
                log.warn("No header fields found for segment: {}, using empty list", segmentName);
                headerFields = new java.util.ArrayList<>();
            }
            if (tableColumns == null || tableColumns.isEmpty()) {
                log.warn("No table columns found for segment: {}, using default columns", segmentName);
                // Create default columns if none found
                tableColumns = new java.util.ArrayList<>();
                java.util.Map<String, Object> col1 = new java.util.HashMap<>();
                col1.put("label", "Date");
                col1.put("key", "date");
                col1.put("width", 20);
                tableColumns.add(col1);
                
                java.util.Map<String, Object> col2 = new java.util.HashMap<>();
                col2.put("label", "Description");
                col2.put("key", "description");
                col2.put("width", 50);
                tableColumns.add(col2);
                
                java.util.Map<String, Object> col3 = new java.util.HashMap<>();
                col3.put("label", "Debit");
                col3.put("key", "debit");
                col3.put("width", 15);
                tableColumns.add(col3);
                
                java.util.Map<String, Object> col4 = new java.util.HashMap<>();
                col4.put("label", "Credit");
                col4.put("key", "credit");
                col4.put("width", 15);
                tableColumns.add(col4);
            }
            
            java.awt.Color primaryColorAwt;
            java.awt.Color secondaryColorAwt;
            
            try {
                primaryColorAwt = colorUtil.getPrimaryColor(segmentName);
                secondaryColorAwt = colorUtil.getSecondaryColor(segmentName);
                log.info("Excel generation - Primary color: RGB({}, {}, {}), Secondary color: RGB({}, {}, {})", 
                        primaryColorAwt.getRed(), primaryColorAwt.getGreen(), primaryColorAwt.getBlue(),
                        secondaryColorAwt.getRed(), secondaryColorAwt.getGreen(), secondaryColorAwt.getBlue());
            } catch (Exception e) {
                log.warn("Error getting colors for segment {}, using defaults: {}", segmentName, e.getMessage());
                primaryColorAwt = new java.awt.Color(30, 58, 138); // Default blue
                secondaryColorAwt = new java.awt.Color(59, 130, 246); // Default light blue
            }
            
            // Create XSSFColor using proper constructor
            XSSFColor primaryColor = new XSSFColor(
                new byte[]{(byte)primaryColorAwt.getRed(), (byte)primaryColorAwt.getGreen(), (byte)primaryColorAwt.getBlue()}, 
                null
            );
            XSSFColor secondaryColor = new XSSFColor(
                new byte[]{(byte)secondaryColorAwt.getRed(), (byte)secondaryColorAwt.getGreen(), (byte)secondaryColorAwt.getBlue()}, 
                null
            );
            
            // Validate statement data
            if (statementData == null) {
                throw new IOException("Statement data is null");
            }
            
            if (statementData.getHeaderData() == null) {
                log.warn("Header data is null, using empty map");
                statementData.setHeaderData(new java.util.HashMap<>());
            }
            
            int totalRows = statementData.getTransactions() != null ? statementData.getTransactions().size() : 0;
            log.info("Total transactions to process: {}", totalRows);
            
            int currentIndex = 0;
            int sheetNumber = 1;
            
            String headerPath = "Private".equalsIgnoreCase(segmentName) ? privateHeaderImagePath : headerImagePath;
            String footerPath = "Private".equalsIgnoreCase(segmentName) ? privateFooterImagePath : footerImagePath;
            log.info("Loading header image from: {}", headerPath);
            log.info("Loading footer image from: {}", footerPath);
            byte[] headerImageBytes = loadImageBytes(headerPath);
            byte[] footerImageBytes = loadImageBytes(footerPath);
            
            if (headerImageBytes == null) {
                log.warn("Header image not found at: {}", headerPath);
            }
            if (footerImageBytes == null) {
                log.warn("Footer image not found at: {}", footerPath);
            }
            
            // Always create at least one sheet, even if no transactions
            int numColumns = tableColumns != null && !tableColumns.isEmpty() ? tableColumns.size() : 4; // Default to 4 columns if empty
            
            // Ensure we have at least one sheet - always create at least one
            boolean createSheet = true;
            
            while (createSheet) {
                try {
                    String sheetName = "Statement - Page " + sheetNumber;
                    // Excel sheet names have a 31 character limit and cannot contain certain characters
                    if (sheetName.length() > 31) {
                        sheetName = "Statement-P" + sheetNumber;
                    }
                    Sheet sheet = workbook.createSheet(sheetName);
                    log.debug("Creating sheet '{}' with {} columns", sheetName, numColumns);
                    
                    int rowNum = 0;
                    
                    if (headerImageBytes != null && numColumns > 0) {
                        try {
                            addHeaderImage(sheet, workbook, headerImageBytes, numColumns);
                            rowNum += 2;
                        } catch (Exception e) {
                            log.warn("Failed to add header image: {}", e.getMessage());
                        }
                    }
                    
                    if (numColumns > 0) {
                        Row titleRow = sheet.createRow(rowNum++);
                        Cell titleCell = titleRow.createCell(0);
                        titleCell.setCellValue("Account Statement");
                        CellStyle titleStyle = workbook.createCellStyle();
                        Font titleFont = workbook.createFont();
                        titleFont.setBold(true);
                        titleFont.setFontHeightInPoints((short) 16);
                        titleFont.setColor(IndexedColors.WHITE.getIndex());
                        titleStyle.setFont(titleFont);
                        if (titleStyle instanceof XSSFCellStyle) {
                            ((XSSFCellStyle) titleStyle).setFillForegroundColor(primaryColor);
                        } else {
                            titleStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                        }
                        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        titleStyle.setAlignment(HorizontalAlignment.CENTER);
                        titleCell.setCellStyle(titleStyle);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, numColumns - 1));
                        titleRow.setHeightInPoints(30);
                    }
                    
                    rowNum++;
                    
                    if (headerFields != null && !headerFields.isEmpty()) {
                        for (Map<String, Object> field : headerFields) {
                            Row headerRow = sheet.createRow(rowNum++);
                            
                            Cell labelCell = headerRow.createCell(0);
                            labelCell.setCellValue(field.get("label") + ":");
                            CellStyle labelStyle = workbook.createCellStyle();
                            Font labelFont = workbook.createFont();
                            labelFont.setBold(true);
                            labelFont.setFontHeightInPoints((short) 10);
                            labelStyle.setFont(labelFont);
                            if (labelStyle instanceof XSSFCellStyle) {
                                ((XSSFCellStyle) labelStyle).setFillForegroundColor(secondaryColor);
                            } else {
                                labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                            }
                            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            labelStyle.setBorderBottom(BorderStyle.THIN);
                            labelStyle.setBorderTop(BorderStyle.THIN);
                            labelStyle.setBorderLeft(BorderStyle.THIN);
                            labelStyle.setBorderRight(BorderStyle.THIN);
                            labelCell.setCellStyle(labelStyle);
                            
                            Cell valueCell = headerRow.createCell(1);
                            String key = (String) field.get("key");
                            String value = statementData.getHeaderData().getOrDefault(key, "");
                            valueCell.setCellValue(value);
                            CellStyle valueStyle = workbook.createCellStyle();
                            valueStyle.setBorderBottom(BorderStyle.THIN);
                            valueStyle.setBorderTop(BorderStyle.THIN);
                            valueStyle.setBorderLeft(BorderStyle.THIN);
                            valueStyle.setBorderRight(BorderStyle.THIN);
                            valueCell.setCellStyle(valueStyle);
                        }
                    }
                    
                    rowNum++;
                    
                    if (tableColumns != null && !tableColumns.isEmpty()) {
                        Row tableHeaderRow = sheet.createRow(rowNum++);
                        tableHeaderRow.setHeightInPoints(25);
                        
                        int colIndex = 0;
                        for (Map<String, Object> column : tableColumns) {
                            Cell headerCell = tableHeaderRow.createCell(colIndex++);
                            headerCell.setCellValue(column.get("label").toString());
                            CellStyle headerStyle = workbook.createCellStyle();
                            Font headerFont = workbook.createFont();
                            headerFont.setBold(true);
                            headerFont.setFontHeightInPoints((short) 11);
                            headerFont.setColor(IndexedColors.WHITE.getIndex());
                            headerStyle.setFont(headerFont);
                            if (headerStyle instanceof XSSFCellStyle) {
                                ((XSSFCellStyle) headerStyle).setFillForegroundColor(primaryColor);
                            } else {
                                headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                            }
                            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            headerStyle.setAlignment(HorizontalAlignment.CENTER);
                            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                            headerStyle.setBorderBottom(BorderStyle.THIN);
                            headerStyle.setBorderTop(BorderStyle.THIN);
                            headerStyle.setBorderLeft(BorderStyle.THIN);
                            headerStyle.setBorderRight(BorderStyle.THIN);
                            headerCell.setCellStyle(headerStyle);
                        }
                    }
                    
                    int rowsOnThisSheet = 0;
                    CellStyle dataStyle = workbook.createCellStyle();
                    dataStyle.setBorderBottom(BorderStyle.THIN);
                    dataStyle.setBorderTop(BorderStyle.THIN);
                    dataStyle.setBorderLeft(BorderStyle.THIN);
                    dataStyle.setBorderRight(BorderStyle.THIN);
                    
                    CellStyle alternateDataStyle = workbook.createCellStyle();
                    alternateDataStyle.cloneStyleFrom(dataStyle);
                    alternateDataStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    alternateDataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    
                    CellStyle numberStyle = workbook.createCellStyle();
                    numberStyle.cloneStyleFrom(dataStyle);
                    numberStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0.00"));
                    
                    CellStyle alternateNumberStyle = workbook.createCellStyle();
                    alternateNumberStyle.cloneStyleFrom(numberStyle);
                    alternateNumberStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    alternateNumberStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    
                    while (currentIndex < totalRows && rowsOnThisSheet < rowsPerPage) {
                        if (statementData.getTransactions() == null || currentIndex >= statementData.getTransactions().size()) {
                            break;
                        }
                        
                        StatementData.Transaction transaction = statementData.getTransactions().get(currentIndex);
                        Row dataRow = sheet.createRow(rowNum++);
                        
                        boolean isAlternate = rowsOnThisSheet % 2 == 0;
                        int colIndex = 0; // Declare colIndex here for transaction data rows
                        
                        if (tableColumns != null && !tableColumns.isEmpty()) {
                            for (Map<String, Object> column : tableColumns) {
                                String key = column.get("key").toString();
                                Cell dataCell = dataRow.createCell(colIndex++);
                                
                                if (key.equals("debit") || key.equals("credit") || key.equals("balance")) {
                                    String valueStr = getTransactionValue(transaction, key);
                                    if (valueStr != null && !valueStr.isEmpty() && !valueStr.equals("0.00")) {
                                        try {
                                            // Remove commas and parse
                                            String cleanValue = valueStr.replace(",", "");
                                            dataCell.setCellValue(Double.parseDouble(cleanValue));
                                        } catch (NumberFormatException e) {
                                            log.warn("Could not parse number: {}, using as string", valueStr);
                                            dataCell.setCellValue(valueStr);
                                        }
                                    } else {
                                        dataCell.setCellValue(0.0);
                                    }
                                    dataCell.setCellStyle(isAlternate ? alternateNumberStyle : numberStyle);
                                } else {
                                    String value = getTransactionValue(transaction, key);
                                    dataCell.setCellValue(value != null ? value : "");
                                    dataCell.setCellStyle(isAlternate ? alternateDataStyle : dataStyle);
                                }
                            }
                        }
                        
                        currentIndex++;
                        rowsOnThisSheet++;
                    }
                    
                    int actualColumnCount = tableColumns != null ? tableColumns.size() : 4;
                    for (int i = 0; i < actualColumnCount; i++) {
                        sheet.autoSizeColumn(i);
                        int width = sheet.getColumnWidth(i);
                        sheet.setColumnWidth(i, Math.max(width, 2000));
                    }
                    
                    int totalContentWidthEMU = 0;
                    for (int i = 0; i < actualColumnCount; i++) {
                        int colWidthUnits = sheet.getColumnWidth(i);
                        totalContentWidthEMU += (int)(colWidthUnits * 7 * 9525);
                    }
                    
                    int footerStartRow = rowNum + 2;
                    int footerTextRowCount = 0;
                    
                    if (currentIndex >= totalRows && statementData.getFooter() != null) {
                        try {
                            addFooterToSheet(sheet, statementData.getFooter(), footerStartRow, actualColumnCount, primaryColor);
                            StatementData.StatementFooter footer = statementData.getFooter();
                            if (footer.getGeneratedOn() != null) footerTextRowCount++;
                            if (footer.getDisclaimer() != null) footerTextRowCount++;
                            if (footer.getContactInfo() != null) footerTextRowCount++;
                        } catch (Exception e) {
                            log.warn("Failed to add footer text: {}", e.getMessage());
                        }
                    }
                    
                    // Add footer image after footer text
                    if (footerImageBytes != null && actualColumnCount > 0) {
                        try {
                            int footerImageStartRow = footerStartRow + footerTextRowCount + 1; // Add one row spacing
                            log.info("Adding footer image at row: {}, totalContentWidthEMU: {}", footerImageStartRow, totalContentWidthEMU);
                            addFooterImage(sheet, workbook, footerImageBytes, actualColumnCount, footerImageStartRow, totalContentWidthEMU);
                            log.info("Footer image added successfully");
                        } catch (Exception e) {
                            log.error("Failed to add footer image: {}", e.getMessage(), e);
                        }
                    } else {
                        log.warn("Footer image bytes are null or column count is 0. footerImageBytes: {}, actualColumnCount: {}", 
                                footerImageBytes != null, actualColumnCount);
                    }
                    
                    sheetNumber++;
                    
                    // Break if no more transactions to process
                    if (currentIndex >= totalRows) {
                        createSheet = false;
                    }
                } catch (Exception e) {
                    log.error("Error creating sheet {}: {}", sheetNumber, e.getMessage(), e);
                    // Try to continue with next sheet or break if critical error
                    if (workbook.getNumberOfSheets() == 0) {
                        // If we haven't created any sheets yet, this is critical
                        throw new IOException("Failed to create first sheet: " + e.getMessage(), e);
                    }
                    createSheet = false; // Stop trying to create more sheets
                }
            }
            
            // Ensure at least one sheet was created
            if (workbook.getNumberOfSheets() == 0) {
                log.warn("No sheets created, creating empty sheet");
                Sheet emptySheet = workbook.createSheet("Statement");
                Row emptyRow = emptySheet.createRow(0);
                emptyRow.createCell(0).setCellValue("No transactions found");
            }
            
            log.info("Created {} sheet(s) in workbook", workbook.getNumberOfSheets());
            
            log.info("Writing workbook to output stream...");
            workbook.write(baos);
            log.info("Excel generation completed successfully. Size: {} bytes", baos.size());
            
            byte[] result = baos.toByteArray();
            if (result == null || result.length == 0) {
                throw new IOException("Generated Excel file is empty");
            }
            log.info("Returning Excel file with {} bytes", result.length);
            return result;
        } catch (IOException e) {
            log.error("IOException during Excel generation: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Excel generation: ", e);
            throw new IOException("Excel generation failed: " + e.getMessage() + " (Type: " + e.getClass().getSimpleName() + ")", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                    log.debug("Workbook closed successfully");
                } catch (Exception e) {
                    log.warn("Error closing workbook: {}", e.getMessage());
                }
            }
        }
    }
    
    private String getTransactionValue(StatementData.Transaction transaction, String key) {
        switch (key) {
            case "date": return transaction.getDate() != null ? transaction.getDate() : "";
            case "description": return transaction.getDescription() != null ? transaction.getDescription() : "";
            case "reference": return transaction.getReference() != null ? transaction.getReference() : "";
            case "debit": return transaction.getDebit() != null ? transaction.getDebit() : "0.00";
            case "credit": return transaction.getCredit() != null ? transaction.getCredit() : "0.00";
            case "balance": return transaction.getBalance() != null ? transaction.getBalance() : "0.00";
            default: return "";
        }
    }
    
    private void addFooterToSheet(Sheet sheet, StatementData.StatementFooter footer, int startRow, int numColumns, XSSFColor primaryColor) {
        if (footer == null) return;
        
        CellStyle footerStyle = sheet.getWorkbook().createCellStyle();
        Font footerFont = sheet.getWorkbook().createFont();
        footerFont.setFontHeightInPoints((short) 9);
        footerStyle.setFont(footerFont);
        footerStyle.setAlignment(HorizontalAlignment.CENTER);
        footerStyle.setBorderTop(BorderStyle.THIN);
        
        if (footerStyle instanceof XSSFCellStyle) {
            ((XSSFCellStyle) footerStyle).setTopBorderColor(primaryColor);
        }
        
        int rowNum = startRow;
        
        if (footer.getGeneratedOn() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue("Generated on: " + footer.getGeneratedOn());
            cell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, numColumns - 1));
        }
        
        if (footer.getDisclaimer() != null) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(footer.getDisclaimer());
            cell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, numColumns - 1));
        }
        
        if (footer.getContactInfo() != null) {
            Row row = sheet.createRow(rowNum);
            Cell cell = row.createCell(0);
            cell.setCellValue(footer.getContactInfo());
            cell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, numColumns - 1));
        }
    }
    
    private byte[] loadImageBytes(String imagePath) {
        try {
            var resource = resourceLoader.getResource("classpath:" + imagePath);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    return IOUtils.toByteArray(inputStream);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load image {}: {}", imagePath, e.getMessage());
        }
        return null;
    }
    
    private void addHeaderImage(Sheet sheet, XSSFWorkbook workbook, byte[] imageBytes, int numColumns) {
        try {
            int minColumnWidthUnits = 2000;
            int totalContentWidthEMU = (int)(numColumns * minColumnWidthUnits * 7 * 9525);
            
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            
            int fixedWidthEMU = totalContentWidthEMU;
            int fixedHeightEMU = 1371600;
            
            int startCol = 0;
            int endCol = numColumns - 1;
            
            XSSFClientAnchor anchor = new XSSFClientAnchor(
                0, 0, fixedWidthEMU, fixedHeightEMU,
                startCol, 0, endCol, 1
            );
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            
            XSSFPicture picture = (XSSFPicture) drawing.createPicture(anchor, pictureIdx);
            
            for (int col = startCol; col <= endCol; col++) {
                Row row0 = sheet.getRow(0);
                if (row0 == null) row0 = sheet.createRow(0);
                Cell cell0 = row0.getCell(col);
                if (cell0 == null) cell0 = row0.createCell(col);
                CellStyle borderStyle = workbook.createCellStyle();
                borderStyle.setBorderTop(BorderStyle.THIN);
                borderStyle.setBorderBottom(BorderStyle.THIN);
                borderStyle.setBorderLeft(col == startCol ? BorderStyle.THIN : BorderStyle.NONE);
                borderStyle.setBorderRight(col == endCol ? BorderStyle.THIN : BorderStyle.NONE);
                cell0.setCellStyle(borderStyle);
                
                Row row1 = sheet.getRow(1);
                if (row1 == null) row1 = sheet.createRow(1);
                Cell cell1 = row1.getCell(col);
                if (cell1 == null) cell1 = row1.createCell(col);
                CellStyle borderStyle1 = workbook.createCellStyle();
                borderStyle1.setBorderBottom(BorderStyle.THIN);
                borderStyle1.setBorderLeft(col == startCol ? BorderStyle.THIN : BorderStyle.NONE);
                borderStyle1.setBorderRight(col == endCol ? BorderStyle.THIN : BorderStyle.NONE);
                cell1.setCellStyle(borderStyle1);
            }
            
            if (sheet.getRow(0) != null) sheet.getRow(0).setHeightInPoints(60);
            if (sheet.getRow(1) != null) sheet.getRow(1).setHeightInPoints(60);
        } catch (Exception e) {
            log.warn("Could not add header image: {}", e.getMessage());
        }
    }
    
    private void addFooterImage(Sheet sheet, XSSFWorkbook workbook, byte[] imageBytes, int numColumns, int startRow, int totalContentWidthEMU) {
        try {
            log.info("Adding footer image - startRow: {}, numColumns: {}, totalContentWidthEMU: {}, imageBytes length: {}", 
                    startRow, numColumns, totalContentWidthEMU, imageBytes != null ? imageBytes.length : 0);
            
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Footer image bytes are null or empty");
                return;
            }
            
            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);
            log.info("Picture added to workbook with index: {}", pictureIdx);
            
            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            if (drawing == null) {
                log.error("Failed to create drawing patriarch");
                return;
            }
            
            // Calculate image dimensions based on content width
            int fixedWidthEMU = totalContentWidthEMU > 0 ? totalContentWidthEMU : (int)(numColumns * 2000 * 7 * 9525);
            // Height for footer image (approximately 2 rows)
            int fixedHeightEMU = 1371600;
            
            int footerRow = startRow;
            int startCol = 0;
            int endCol = numColumns - 1;
            
            log.info("Creating anchor - startCol: {}, endCol: {}, footerRow: {}, widthEMU: {}, heightEMU: {}", 
                    startCol, endCol, footerRow, fixedWidthEMU, fixedHeightEMU);
            
            XSSFClientAnchor anchor = new XSSFClientAnchor(
                0, 0, fixedWidthEMU, fixedHeightEMU,
                startCol, footerRow, endCol, footerRow + 2  // Span 2 rows for footer image
            );
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            
            XSSFPicture picture = (XSSFPicture) drawing.createPicture(anchor, pictureIdx);
            if (picture != null) {
                log.info("Footer image picture created successfully");
            }
            
            // Ensure rows exist and have proper height
            for (int rowIdx = footerRow; rowIdx <= footerRow + 1; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                    log.info("Created row {} for footer image", rowIdx);
                }
                row.setHeightInPoints(60); // Set row height to accommodate image
                
                // Create cells and set borders
                for (int col = startCol; col <= endCol; col++) {
                    Cell cell = row.getCell(col);
                    if (cell == null) {
                        cell = row.createCell(col);
                    }
                    CellStyle borderStyle = workbook.createCellStyle();
                    borderStyle.setBorderTop(BorderStyle.THIN);
                    borderStyle.setBorderBottom(BorderStyle.THIN);
                    borderStyle.setBorderLeft(col == startCol ? BorderStyle.THIN : BorderStyle.NONE);
                    borderStyle.setBorderRight(col == endCol ? BorderStyle.THIN : BorderStyle.NONE);
                    cell.setCellStyle(borderStyle);
                }
            }
            
            log.info("Footer image added successfully at row {}", footerRow);
        } catch (Exception e) {
            log.error("Could not add footer image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add footer image: " + e.getMessage(), e);
        }
    }
}

