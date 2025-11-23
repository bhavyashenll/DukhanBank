package com.example.api.account.service.impl;

import com.example.api.account.domain.dto.StatementData;
import com.example.api.account.infrastructure.config.StatementConfigService;
import com.example.api.account.infrastructure.util.StatementColorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatementPdfGenerationServiceImpl implements com.example.api.account.service.StatementPdfGenerationService {
    
    private final StatementConfigService configService;
    private final StatementColorUtil colorUtil;
    private final ResourceLoader resourceLoader;
    
    @Value("${statement.pdf.rows.per.page:25}")
    private int rowsPerPage;
    
    @Value("${statement.pdf.header.image.path:images/DukhanBank_Statement_Header.jpg}")
    private String headerImagePath;
    
    @Value("${statement.pdf.footer.image.path:images/DukhanBank_Statement_Footer.jpg}")
    private String footerImagePath;
    
    @Value("${statement.pdf.header.image.private.path:images/Private_DukhanBank_Statement_Header.jpg}")
    private String privateHeaderImagePath;
    
    @Value("${statement.pdf.footer.image.private.path:images/Private_DukhanBank_Statement_Footer.jpg}")
    private String privateFooterImagePath;
    
    @Value("${statement.pdf.margin.top:72}")
    private float marginTop;
    
    @Value("${statement.pdf.margin.bottom:72}")
    private float marginBottom;
    
    @Value("${statement.pdf.margin.left:72}")
    private float marginLeft;
    
    @Value("${statement.pdf.margin.right:72}")
    private float marginRight;
    
    private static final float A4_WIDTH = 595;
    private static final float A4_HEIGHT = 842;
    
    public StatementPdfGenerationServiceImpl(StatementConfigService configService, StatementColorUtil colorUtil, ResourceLoader resourceLoader) {
        this.configService = configService;
        this.colorUtil = colorUtil;
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public byte[] generatePdf(String segmentName, StatementData statementData) throws IOException {
        PDDocument document = new PDDocument();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            List<Map<String, Object>> headerFields = configService.getHeaderFields(segmentName);
            List<Map<String, Object>> tableColumns = configService.getTableColumns(segmentName);
            
            Color primaryColor = colorUtil.getPrimaryColor(segmentName);
            
            String headerPath = "Private".equalsIgnoreCase(segmentName) ? privateHeaderImagePath : headerImagePath;
            String footerPath = "Private".equalsIgnoreCase(segmentName) ? privateFooterImagePath : footerImagePath;
            PDImageXObject headerImage = loadImage(headerPath, document);
            PDImageXObject footerImage = loadImage(footerPath, document);
            
            float headerHeight = 0;
            float footerHeight = 0;
            float headerWidth = A4_WIDTH;
            float footerWidth = A4_WIDTH;
            
            if (headerImage != null) {
                headerHeight = calculateImageHeight(headerImage, headerWidth);
            }
            if (footerImage != null) {
                footerHeight = calculateImageHeight(footerImage, footerWidth);
            }
            
            // Calculate content area accounting for header and footer
            float contentEndY = footerHeight + marginBottom;
            
            List<StatementData.Transaction> transactions = statementData.getTransactions();
            if (transactions == null || transactions.isEmpty()) {
                transactions = new java.util.ArrayList<>();
            }
            
            int currentIndex = 0;
            int firstPageRows = 19;
            int subsequentPageRows = 28;
            
            PDPage firstPage = new PDPage(PDRectangle.A4);
            document.addPage(firstPage);
            PDPageContentStream contentStream = new PDPageContentStream(document, firstPage);
            
            // Draw header image at the top of the page
            if (headerImage != null) {
                log.info("Drawing header image: {} at position (0, {}) with size ({}, {})", 
                        headerPath, A4_HEIGHT - headerHeight, headerWidth, headerHeight);
                contentStream.drawImage(headerImage, 0, A4_HEIGHT - headerHeight, headerWidth, headerHeight);
            } else {
                log.warn("Header image not found at path: {}", headerPath);
            }
            
            // Add account details below header image
            float accountDetailsY = A4_HEIGHT - headerHeight - 20; // 20 points spacing below header
            float currentY = addAccountDetails(contentStream, headerFields, statementData.getHeaderData(), 
                    accountDetailsY, A4_WIDTH - marginLeft - marginRight, primaryColor);
            
            currentY = addTableHeader(contentStream, tableColumns, currentY - 15, 
                    A4_WIDTH - marginLeft - marginRight, primaryColor, document);
            
            TransactionResult result = addTransactions(contentStream, transactions, tableColumns, currentIndex, 
                    currentY, contentEndY + 20, A4_WIDTH - marginLeft - marginRight, firstPageRows);
            currentIndex = result.nextIndex;
            currentY = result.currentY;
            
            contentStream.close();
            
            while (currentIndex < transactions.size()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                
                // Draw header image on subsequent pages
                if (headerImage != null) {
                    log.debug("Drawing header image on page {}", document.getNumberOfPages());
                    contentStream.drawImage(headerImage, 0, A4_HEIGHT - headerHeight, headerWidth, headerHeight);
                }
                
                // Start table header below header image
                float tableHeaderY = A4_HEIGHT - headerHeight - 20;
                currentY = addTableHeader(contentStream, tableColumns, tableHeaderY, 
                        A4_WIDTH - marginLeft - marginRight, primaryColor, document);
                
                result = addTransactions(contentStream, transactions, tableColumns, currentIndex, 
                        currentY, contentEndY + 20, A4_WIDTH - marginLeft - marginRight, subsequentPageRows);
                currentIndex = result.nextIndex;
                currentY = result.currentY;
                
                contentStream.close();
            }
            
            int totalPages = document.getNumberOfPages();
            
            // Add footer image and page numbers to all pages
            for (int i = 0; i < totalPages; i++) {
                PDPage page = document.getPage(i);
                PDPageContentStream footerStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.APPEND, true, true);
                
                // Draw footer image at the bottom of the page
                if (footerImage != null) {
                    log.debug("Drawing footer image on page {} at position (0, 0) with size ({}, {})", 
                            i + 1, footerWidth, footerHeight);
                    footerStream.drawImage(footerImage, 0, 0, footerWidth, footerHeight);
                } else {
                    log.warn("Footer image not found at path: {}", footerPath);
                }
                
                // Add page number above footer image
                footerStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                footerStream.setNonStrokingColor(Color.BLACK);
                footerStream.beginText();
                footerStream.newLineAtOffset(A4_WIDTH - marginRight - 50, footerHeight + 15);
                footerStream.showText(String.format("Page %d of %d", i + 1, totalPages));
                footerStream.endText();
                
                footerStream.close();
            }
            
            document.save(baos);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    private PDImageXObject loadImage(String imagePath, PDDocument document) {
        try {
            // Load image from classpath resources/images directory
            var resource = resourceLoader.getResource("classpath:" + imagePath);
            if (resource.exists() && resource.isReadable()) {
                log.info("Loading image from: {}", imagePath);
                BufferedImage bufferedImage = ImageIO.read(resource.getInputStream());
                if (bufferedImage != null) {
                    PDImageXObject image = LosslessFactory.createFromImage(document, bufferedImage);
                    log.info("Successfully loaded image: {} ({}x{})", imagePath, 
                            bufferedImage.getWidth(), bufferedImage.getHeight());
                    return image;
                } else {
                    log.warn("Could not read image data from: {}", imagePath);
                }
            } else {
                log.warn("Image resource not found or not readable: {}", imagePath);
            }
        } catch (Exception e) {
            log.error("Error loading image {}: {}", imagePath, e.getMessage(), e);
        }
        return null;
    }
    
    private float calculateImageHeight(PDImageXObject image, float targetWidth) {
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        float scale = targetWidth / imageWidth;
        return imageHeight * scale;
    }
    
    private float addAccountDetails(PDPageContentStream contentStream, List<Map<String, Object>> headerFields,
                                   Map<String, String> headerData, float startY, float width, Color primaryColor) throws IOException {
        log.info("Adding account details to PDF. Header data keys: {}", headerData.keySet());
        log.info("Header data values - Currency: {}, CustomerNo: {}, AccountNo: {}, IBAN: {}, CurrentBalance: {}, AvailableBalance: {}", 
                headerData.get("currency"), headerData.get("customerNo"), headerData.get("accountNumber"), 
                headerData.get("iban"), headerData.get("currentBalance"), headerData.get("availableBalance"));
        
        PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float fontSize = 9;
        contentStream.setFont(regularFont, fontSize);
        contentStream.setNonStrokingColor(Color.BLACK);
        
        float lineHeight = 16;
        float leftColumnX = marginLeft;
        // Right column: labels start at 60% of width, values are right-aligned
        float rightColumnLabelX = marginLeft + width * 0.40f;
        float rightColumnValueX = marginLeft + width;
        
        float leftY = startY;
        float rightY = startY;
        
        for (Map<String, Object> field : headerFields) {
            String key = (String) field.get("key");
            String label = (String) field.get("label");
            String value = headerData.getOrDefault(key, "");
            String position = (String) field.getOrDefault("position", "left");
            String type = (String) field.getOrDefault("type", "");
            Boolean bold = (Boolean) field.getOrDefault("bold", false);
            
            // Log for debugging
            if (key != null && (key.equals("currency") || key.equals("customerNo") || key.equals("accountNumber") 
                    || key.equals("iban") || key.equals("currentBalance") || key.equals("availableBalance"))) {
                log.info("Processing field - Key: {}, Label: {}, Value: {}, Position: {}", key, label, value, position);
            }
            
            boolean isRightField = "right".equalsIgnoreCase(position);
            boolean isBoldValue = Boolean.TRUE.equals(bold);
            boolean isGreeting = "greeting".equals(type);
            boolean isSubtitle = "subtitle".equals(type);
            
            if (isRightField) {
                // Display label (left-aligned in right column)
                contentStream.setFont(regularFont, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(rightColumnLabelX, rightY);
                contentStream.showText(label + " ");
                contentStream.endText();
                
                // Display value (right-aligned, bold if specified)
                if (value == null || value.isEmpty()) {
                    log.warn("Empty value for field: {} (key: {})", label, key);
                    value = ""; // Ensure it's not null
                }
                
                PDType1Font valueFont = isBoldValue ? boldFont : regularFont;
                contentStream.setFont(valueFont, fontSize);
                
                // Calculate value width and position it right-aligned
                float valueWidth = value.isEmpty() ? 0 : valueFont.getStringWidth(value) / 1000 * fontSize;
                float valueX = rightColumnValueX - valueWidth - 5; // 5 points padding from right edge
                contentStream.beginText();
                contentStream.newLineAtOffset(valueX, rightY);
                if (!value.isEmpty()) {
                    contentStream.showText(value);
                }
                contentStream.endText();
                
                rightY -= lineHeight;
            } else {
                // Handle left column fields
                if (isGreeting) {
                    // Greeting: Display the full value (e.g., "Dear 67890")
                    contentStream.setFont(regularFont, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(leftColumnX, leftY);
                    contentStream.showText(value);
                    contentStream.endText();
                    leftY -= lineHeight;
                } else if (isSubtitle) {
                    // Subtitle: Display the full value
                    contentStream.setFont(regularFont, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(leftColumnX, leftY);
                    contentStream.showText(value);
                    contentStream.endText();
                    leftY -= lineHeight;
                } else {
                    // Regular left field: Display value only
                    contentStream.setFont(regularFont, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(leftColumnX, leftY);
                    contentStream.showText(value);
                    contentStream.endText();
                    leftY -= lineHeight;
                }
            }
        }
        
        return Math.min(leftY, rightY);
    }
    
    private float addTableHeader(PDPageContentStream contentStream, List<Map<String, Object>> tableColumns,
                                float startY, float width, Color primaryColor, PDDocument document) throws IOException {
        float[] columnWidths = new float[tableColumns.size()];
        float totalWidth = 0;
        for (int i = 0; i < tableColumns.size(); i++) {
            columnWidths[i] = ((Number) tableColumns.get(i).get("width")).floatValue();
            totalWidth += columnWidths[i];
        }
        
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = (columnWidths[i] / totalWidth) * width;
        }
        
        float headerHeight = 25;
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float fontSize = 9;
        
        // Draw header background using primary color (blue for retail, grey for private)
        contentStream.setNonStrokingColor(primaryColor);
        contentStream.addRect(marginLeft, startY - headerHeight, width, headerHeight);
        contentStream.fill();
        
        // Draw header border
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(marginLeft, startY - headerHeight, width, headerHeight);
        contentStream.stroke();
        
        // Draw column dividers
        float colX = marginLeft;
        for (int i = 0; i < tableColumns.size() - 1; i++) {
            colX += columnWidths[i];
            contentStream.setStrokingColor(Color.WHITE);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(colX, startY);
            contentStream.lineTo(colX, startY - headerHeight);
            contentStream.stroke();
        }
        
        // Draw header text (white, bold)
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.setFont(boldFont, fontSize);
        float currentX = marginLeft;
        
        for (int i = 0; i < tableColumns.size(); i++) {
            Map<String, Object> column = tableColumns.get(i);
            String label = column.get("label").toString();
            float colWidth = columnWidths[i];
            String key = column.get("key").toString();
            
            // Align based on column type
            float textX;
            float textY = startY - headerHeight / 2 - 3; // Vertically centered
            
            if (key.equals("debit") || key.equals("credit")) {
                // Right-align for Debit and Credit
                float textWidth = boldFont.getStringWidth(label) / 1000 * fontSize;
                textX = currentX + colWidth - textWidth - 5;
            } else {
                // Left-align for Date and Description
                textX = currentX + 5;
            }
            
            contentStream.beginText();
            contentStream.newLineAtOffset(textX, textY);
            contentStream.showText(label);
            contentStream.endText();
            
            currentX += colWidth;
        }
        
        return startY - headerHeight;
    }
    
    private PDType0Font loadArabicFont(PDDocument document) {
        try {
            var resource = resourceLoader.getResource("classpath:fonts/arabic-font.ttf");
            if (resource.exists()) {
                return PDType0Font.load(document, resource.getInputStream());
            }
        } catch (Exception e) {
            log.debug("Arabic font not found: {}", e.getMessage());
        }
        return null;
    }
    
    private String getArabicLabel(String englishLabel) {
        switch (englishLabel.toLowerCase()) {
            case "date": return "التاريخ";
            case "description": return "البيان";
            case "reference": return "المرجع";
            case "debit": return "مدين";
            case "credit": return "دائن";
            case "balance": return "الرصيد";
            default: return englishLabel;
        }
    }
    
    private TransactionResult addTransactions(PDPageContentStream contentStream, List<StatementData.Transaction> transactions,
                                             List<Map<String, Object>> tableColumns, int startIndex, float startY,
                                             float endY, float width, int maxRows) throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float fontSize = 9;
        contentStream.setFont(font, fontSize);
        
        float[] columnWidths = new float[tableColumns.size()];
        float totalWidth = 0;
        for (int i = 0; i < tableColumns.size(); i++) {
            columnWidths[i] = ((Number) tableColumns.get(i).get("width")).floatValue();
            totalWidth += columnWidths[i];
        }
        
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = (columnWidths[i] / totalWidth) * width;
        }
        
        float currentY = startY;
        float baseRowHeight = 25; // Increased base height for better clarity
        float lineHeight = fontSize + 4; // Increased line height for better spacing
        int rowIndex = 0;
        int transactionIndex = startIndex;
        float tableStartY = startY;
        
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(0.5f);
        
        while (transactionIndex < transactions.size() && currentY > endY && rowIndex < maxRows) {
            StatementData.Transaction transaction = transactions.get(transactionIndex);
            
            // Calculate required row height based on description text wrapping
            String description = getTransactionValue(transaction, "description");
            float descriptionWidth = columnWidths[getColumnIndex(tableColumns, "description")] - 10; // Subtract padding
            List<String> descriptionLines = wrapText(description, descriptionWidth, font, fontSize);
            // Add extra padding for multi-line descriptions
            float requiredRowHeight = Math.max(baseRowHeight, (descriptionLines.size() * lineHeight) + 6);
            
            // Check if we have enough space for this row
            if (currentY - requiredRowHeight < endY) {
                break; // Not enough space, stop here
            }
            
            // Draw background for alternating rows
            if (rowIndex % 2 == 0) {
                contentStream.setNonStrokingColor(new Color(245, 245, 245));
                contentStream.addRect(marginLeft, currentY - requiredRowHeight, width, requiredRowHeight);
                contentStream.fill();
            }
            
            // Draw all borders for the row (top, bottom, left, right, and column dividers)
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(0.5f);
            
            // Top border
            contentStream.moveTo(marginLeft, currentY);
            contentStream.lineTo(marginLeft + width, currentY);
            contentStream.stroke();
            
            // Bottom border
            contentStream.moveTo(marginLeft, currentY - requiredRowHeight);
            contentStream.lineTo(marginLeft + width, currentY - requiredRowHeight);
            contentStream.stroke();
            
            // Left border
            contentStream.moveTo(marginLeft, currentY);
            contentStream.lineTo(marginLeft, currentY - requiredRowHeight);
            contentStream.stroke();
            
            // Right border
            contentStream.moveTo(marginLeft + width, currentY);
            contentStream.lineTo(marginLeft + width, currentY - requiredRowHeight);
            contentStream.stroke();
            
            // Draw column borders (vertical dividers)
            float cellX = marginLeft;
            for (int j = 0; j < tableColumns.size() - 1; j++) {
                cellX += columnWidths[j];
                contentStream.moveTo(cellX, currentY);
                contentStream.lineTo(cellX, currentY - requiredRowHeight);
                contentStream.stroke();
            }
            
            // Draw cell content
            float currentX = marginLeft;
            contentStream.setNonStrokingColor(Color.BLACK);
            
            for (int j = 0; j < tableColumns.size(); j++) {
                Map<String, Object> column = tableColumns.get(j);
                String key = column.get("key").toString();
                String value = getTransactionValue(transaction, key);
                float colWidth = columnWidths[j];
                
                if (key.equals("description")) {
                    // Handle multi-line text for description (left-aligned)
                    float textX = currentX + 5;
                    // Center text vertically in the cell
                    float textY = currentY - (requiredRowHeight / 2) + ((descriptionLines.size() - 1) * lineHeight / 2);
                    
                    for (int lineIndex = 0; lineIndex < descriptionLines.size(); lineIndex++) {
                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(textX, textY - (lineIndex * lineHeight));
                        contentStream.showText(descriptionLines.get(lineIndex));
                        contentStream.endText();
                    }
                } else {
                    // Single line text for other columns
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    
                    // Center text vertically in the cell
                    float textY = currentY - requiredRowHeight / 2 - 3;
                    
                    if (key.equals("debit") || key.equals("credit")) {
                        // Right-align for Debit and Credit columns
                        float textWidth = font.getStringWidth(value) / 1000 * fontSize;
                        float textX = currentX + colWidth - textWidth - 5;
                        contentStream.newLineAtOffset(textX, textY);
                    } else {
                        // Left-align for Date column
                        contentStream.newLineAtOffset(currentX + 5, textY);
                    }
                    
                    contentStream.showText(value);
                    contentStream.endText();
                }
                
                currentX += colWidth;
            }
            
            currentY -= requiredRowHeight;
            rowIndex++;
            transactionIndex++;
        }
        
        if (rowIndex > 0) {
            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(marginLeft, currentY);
            contentStream.lineTo(marginLeft + width, currentY);
            contentStream.stroke();
            contentStream.moveTo(marginLeft, tableStartY);
            contentStream.lineTo(marginLeft, currentY);
            contentStream.stroke();
            contentStream.moveTo(marginLeft + width, tableStartY);
            contentStream.lineTo(marginLeft + width, currentY);
            contentStream.stroke();
        }
        
        return new TransactionResult(currentY, transactionIndex);
    }
    
    private String getTransactionValue(StatementData.Transaction transaction, String key) {
        switch (key) {
            case "date": 
                String date = transaction.getDate() != null ? transaction.getDate() : "";
                // Format date to YYYY-MM-DD if needed
                if (!date.isEmpty() && !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    // Try to convert from other formats
                    date = formatDateToYYYYMMDD(date);
                }
                return date;
            case "description": 
                return transaction.getDescription() != null ? transaction.getDescription() : "";
            case "reference": 
                return transaction.getReference() != null ? transaction.getReference() : "";
            case "debit": 
                // Always show "0.00" if null or empty (as per image)
                String debit = transaction.getDebit();
                if (debit == null || debit.isEmpty() || debit.equals("0") || debit.equals("0.0") || debit.equals("0.00")) {
                    return "0.00";
                }
                return debit;
            case "credit": 
                // Always show "0.00" if null or empty (as per image)
                String credit = transaction.getCredit();
                if (credit == null || credit.isEmpty() || credit.equals("0") || credit.equals("0.0") || credit.equals("0.00")) {
                    return "0.00";
                }
                return credit;
            case "balance": 
                return transaction.getBalance() != null ? transaction.getBalance() : "0.00";
            default: 
                return "";
        }
    }
    
    /**
     * Format date to YYYY-MM-DD format
     */
    private String formatDateToYYYYMMDD(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        
        try {
            // Try common date formats
            java.text.SimpleDateFormat[] formats = {
                new java.text.SimpleDateFormat("dd/MM/yyyy"),
                new java.text.SimpleDateFormat("MM/dd/yyyy"),
                new java.text.SimpleDateFormat("yyyy-MM-dd"),
                new java.text.SimpleDateFormat("dd-MM-yyyy"),
                new java.text.SimpleDateFormat("yyyy/MM/dd")
            };
            
            for (java.text.SimpleDateFormat format : formats) {
                try {
                    java.util.Date parsedDate = format.parse(date);
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return outputFormat.format(parsedDate);
                } catch (Exception e) {
                    // Try next format
                }
            }
        } catch (Exception e) {
            log.warn("Could not format date: {}", date, e);
        }
        
        // Return as-is if can't parse
        return date;
    }
    
    /**
     * Wrap text to fit within the specified width
     */
    private List<String> wrapText(String text, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        
        // Calculate character width
        float avgCharWidth = font.getStringWidth("M") / 1000 * fontSize;
        int maxCharsPerLine = (int) (maxWidth / avgCharWidth);
        
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 
                ? currentLine.toString() + " " + word 
                : word;
            
            // Check if adding this word would exceed the width
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;
            
            if (testWidth <= maxWidth && testLine.length() <= maxCharsPerLine * 1.5) {
                // Word fits, add it to current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Word doesn't fit, start a new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, split it
                    if (word.length() > maxCharsPerLine) {
                        // Split long word
                        int start = 0;
                        while (start < word.length()) {
                            int end = Math.min(start + maxCharsPerLine, word.length());
                            lines.add(word.substring(start, end));
                            start = end;
                        }
                    } else {
                        lines.add(word);
                    }
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        if (lines.isEmpty()) {
            lines.add("");
        }
        
        return lines;
    }
    
    /**
     * Get the index of a column by its key
     */
    private int getColumnIndex(List<Map<String, Object>> tableColumns, String key) {
        for (int i = 0; i < tableColumns.size(); i++) {
            if (key.equals(tableColumns.get(i).get("key"))) {
                return i;
            }
        }
        return 0; // Default to first column if not found
    }
    
    private static class TransactionResult {
        float currentY;
        int nextIndex;
        
        TransactionResult(float currentY, int nextIndex) {
            this.currentY = currentY;
            this.nextIndex = nextIndex;
        }
    }
}

