package com.example.api.account.controller;

import com.example.api.account.service.StatementDataService;
import com.example.api.account.service.StatementExcelGenerationService;
import com.example.api.account.service.StatementPdfGenerationService;
import com.example.api.account.service.DownloadTransactionsService;
import com.example.api.account.domain.dto.StatementData;
import com.example.api.infrastructure.AppConstant;
import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.account.service.CustomerAccountValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class DownloadTransactionsController {
    
    @Autowired
    private StatementPdfGenerationService pdfGenerationService;
    
    @Autowired
    private StatementExcelGenerationService excelGenerationService;
    
    @Autowired
    private StatementDataService statementDataService;

    @Autowired
    private DownloadTransactionsService downloadTransactionsService;

    @Autowired
    private CustomerAccountValidationService customerAccountValidationService;
    
    @PostMapping("/download-transactions")
    public ResponseEntity<?> downloadAccountStatement(
            @RequestBody BaseServiceRequest baseServiceRequest,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenId,
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String acceptLanguage) {
        try {
            ApiResponse<Map<String, Object>> validationResponse = downloadTransactionsService.validateRequest(baseServiceRequest);
            if (Objects.nonNull(validationResponse)) {
                log.warn("Request validation failed for download-transactions");
                return ResponseEntity.badRequest().body(validationResponse);
            }

            Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
            String accountNumber = Objects.toString(requestInfo.get("accountNumber"), null);
            String segmentName = Objects.toString(requestInfo.get("segmentName"), null);
            String fieldType = Objects.toString(requestInfo.get("fieldType"), null);
            String customerId = Objects.toString(requestInfo.get("customerId"), null);
            
            log.info("Received statement download request - account: {}, segment: {}, type: {}", 
                    accountNumber, segmentName, fieldType);
            
            // Default to "retail" if segmentName is null or empty
            if (Objects.isNull(segmentName) || segmentName.trim().isEmpty()) {
                segmentName = "retail";
            }

            ApiResponse<?> accountValidationResponse = customerAccountValidationService.validate(baseServiceRequest);

            if (Objects.nonNull(accountValidationResponse) && Objects.nonNull(accountValidationResponse.getStatus()) 
                    && !"000000".equals(accountValidationResponse.getStatus().getCode())) {
                log.warn("Customer account validation failed with status code: {}", 
                        accountValidationResponse.getStatus().getCode());
                return ResponseEntity.badRequest().body(accountValidationResponse);
            }           
            
            // Fetch data from existing services
            StatementData statementData;
            try {
                statementData = statementDataService.fetchStatementData(
                        accountNumber, customerId, serviceId, moduleId, subModuleId, screenId, channel, acceptLanguage);
                log.info("Fetched statement data - Header data present: {}, Transactions count: {}", 
                        Objects.nonNull(statementData) && Objects.nonNull(statementData.getHeaderData()) ? statementData.getHeaderData().size() : 0,
                        Objects.nonNull(statementData) && Objects.nonNull(statementData.getTransactions()) ? statementData.getTransactions().size() : 0);
            } catch (Exception e) {
                log.error("Error fetching statement data: ", e);
                ApiResponse<String> errorResponse = ApiResponse.error();
                errorResponse.getStatus().setCode("000500");
                errorResponse.getStatus().setDescription("Failed to fetch account data: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            if (Objects.isNull(statementData)) {
                log.warn("Statement data is null");
                ApiResponse<String> errorResponse = ApiResponse.error();
                errorResponse.getStatus().setCode("000404");
                errorResponse.getStatus().setDescription("No account data found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            if (Objects.isNull(statementData.getTransactions()) || statementData.getTransactions().isEmpty()) {
                log.warn("No transactions found for account: {}", accountNumber);
                ApiResponse<String> errorResponse = ApiResponse.error();
                errorResponse.getStatus().setCode("000404");
                errorResponse.getStatus().setDescription("No transactions found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            if (Objects.isNull(statementData.getHeaderData())) {
                log.warn("Header data is null, initializing empty map");
                statementData.setHeaderData(new java.util.HashMap<>());
            }
            
            byte[] fileContent;
            String contentType;
            String fileExtension;
            
            if (fieldType.equalsIgnoreCase("pdf")) {
                try {
                    fileContent = pdfGenerationService.generatePdf(segmentName, statementData);
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                    fileExtension = "pdf";
                } catch (IOException e) {
                    log.error("PDF generation failed: ", e);
                    ApiResponse<String> errorResponse = ApiResponse.error();
                    errorResponse.getStatus().setCode("000500");
                    errorResponse.getStatus().setDescription("PDF generation failed");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            } else {
                try {
                    log.info("Starting Excel generation for segment: {}", segmentName);
                    fileContent = excelGenerationService.generateExcel(segmentName, statementData);
                    contentType = "application/vnd.ms-excel"; // MIME type for .xls files
                    fileExtension = "xls";
                    log.info("Excel generation completed successfully. File size: {} bytes", fileContent.length);
                } catch (IOException e) {
                    log.error("Excel generation failed: ", e);
                    ApiResponse<String> errorResponse = ApiResponse.error();
                    errorResponse.getStatus().setCode("000500");
                    errorResponse.getStatus().setDescription("Excel generation failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                } catch (Exception e) {
                    log.error("Unexpected error during Excel generation: ", e);
                    ApiResponse<String> errorResponse = ApiResponse.error();
                    errorResponse.getStatus().setCode("000500");
                    errorResponse.getStatus().setDescription("Excel generation failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            }
            
            String fileName = String.format("Account_Statement_%s.%s", segmentName, fileExtension);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileContent.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            log.info("Successfully generated {} file for account: {}, size: {} bytes", fieldType, accountNumber, fileContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
            
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            ApiResponse<String> errorResponse = ApiResponse.error();
            errorResponse.getStatus().setCode("000503");
            errorResponse.getStatus().setDescription("Service unavailable");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

