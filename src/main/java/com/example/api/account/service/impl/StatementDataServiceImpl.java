package com.example.api.account.service.impl;

import com.example.api.account.domain.dto.StatementData;
import com.example.api.account.service.StatementDataService;
import com.example.api.account.service.AccountTransactionService;
import com.example.api.dto.AccountDetailsResponse;
import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.service.AccountDetailService;
import com.example.api.service.ProcessUIRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class StatementDataServiceImpl implements StatementDataService {
    
    @Autowired
    private ProcessUIRequest processUIRequest;
    
    @Autowired
    private AccountDetailService accountDetailService;
    
    @Autowired
    private AccountTransactionService accountTransactionService;
    
    @Override
    public StatementData fetchStatementData(String accountNumber, String customerId, 
                                           String serviceId, String moduleId, String subModuleId,
                                           String screenId, String channel, String lang) {
        log.info("Fetching statement data for account: {}, customer: {}", accountNumber, customerId);
        
        StatementData statementData = new StatementData();
        Map<String, String> headerData = new HashMap<>();
        
        // Fetch account details
        try {
            BaseServiceRequest accountDetailsRequest = createAccountDetailsRequest(accountNumber, customerId);
            ApiResponse<Map<String, Object>> accountDetailsResponse = processUIRequest.processEndToEndFlow(
                    "ACCOUNT.DETAIL", accountDetailsRequest, null);
            
            // Log raw response for debugging
            if (Objects.nonNull(accountDetailsResponse) && Objects.nonNull(accountDetailsResponse.getData()) && !accountDetailsResponse.getData().isEmpty()) {
                Map<String, Object> rawData = accountDetailsResponse.getData().get(0);
                log.info("Raw API response data keys: {}", rawData.keySet());
                log.info("Raw API response - IBAN: {}, CurrentBalance (currnetBalance): {}, CurrentBalance: {}, AvailableBalance: {}", 
                        rawData.get("iban"), rawData.get("currnetBalance"), rawData.get("currentBalance"), rawData.get("availableBalance"));
            }
            
            // Use String lang instead of AccountDetailsContext
            ApiResponse<AccountDetailsResponse> transformed = accountDetailService.postProcessAccountDetails(
                    accountDetailsResponse, lang, accountDetailsRequest);
            
            // Extract from raw response as fallback
            Map<String, Object> rawAccountData = null;
            if (Objects.nonNull(accountDetailsResponse) && Objects.nonNull(accountDetailsResponse.getData()) && !accountDetailsResponse.getData().isEmpty()) {
                rawAccountData = accountDetailsResponse.getData().get(0);
            }
            
            if (Objects.nonNull(transformed) && Objects.nonNull(transformed.getData()) && !transformed.getData().isEmpty()) {
                AccountDetailsResponse accountDetails = transformed.getData().get(0);
                log.info("Transformed account details: IBAN={}, CurrentBalance={}, AvailableBalance={}", 
                        accountDetails.getIban(), accountDetails.getCurrentBalance(), accountDetails.getAvailableBalance());
                
                // Map account details to header data
                headerData.put("accountNumber", Objects.nonNull(accountNumber) ? accountNumber : "");
                headerData.put("customerNo", Objects.nonNull(customerId) ? customerId : "");
                headerData.put("statementDate", getCurrentDateFormatted());
                headerData.put("statementPeriod", getStatementPeriodFormatted());
                headerData.put("accountType", "CK");
                headerData.put("currency", "QAR");
                
                // From API response - IBAN
                String ibanValue = null;
                if (accountDetails.getIban() != null && !accountDetails.getIban().trim().isEmpty()) {
                    ibanValue = accountDetails.getIban().trim();
                } else if (Objects.nonNull(rawAccountData)) {
                    Object ibanObj = rawAccountData.get("iban");
                    if (Objects.nonNull(ibanObj)) {
                        ibanValue = ibanObj.toString().trim();
                        log.info("IBAN extracted from raw response: {}", ibanValue);
                    }
                }
                
                if (Objects.nonNull(ibanValue) && !ibanValue.isEmpty()) {
                    headerData.put("iban", ibanValue);
                } else {
                    headerData.put("iban", "");
                }
                
                // Extract and format Current Balance
                String currentBalanceValue = null;
                if (Objects.nonNull(accountDetails.getCurrentBalance()) && !accountDetails.getCurrentBalance().trim().isEmpty()) {
                    currentBalanceValue = accountDetails.getCurrentBalance();
                } else if (Objects.nonNull(rawAccountData)) {
                    Object currentBalObj = rawAccountData.get("currentBalance");
                    if (Objects.isNull(currentBalObj)) {
                        currentBalObj = rawAccountData.get("currnetBalance");
                    }
                    if (Objects.nonNull(currentBalObj)) {
                        currentBalanceValue = currentBalObj.toString();
                    }
                }
                
                if (Objects.nonNull(currentBalanceValue) && !currentBalanceValue.trim().isEmpty()) {
                    String numericBalance = extractNumericBalance(currentBalanceValue);
                    String formattedBalance = formatBalanceWithCommas(numericBalance);
                    headerData.put("currentBalance", formattedBalance);
                } else {
                    headerData.put("currentBalance", "0.00");
                }
                
                // Extract and format Available Balance
                String availableBalanceValue = null;
                if (Objects.nonNull(accountDetails.getAvailableBalance()) && !accountDetails.getAvailableBalance().trim().isEmpty()) {
                    availableBalanceValue = accountDetails.getAvailableBalance();
                } else if (Objects.nonNull(rawAccountData)) {
                    Object availableBalObj = rawAccountData.get("availableBalance");
                    if (Objects.nonNull(availableBalObj)) {
                        availableBalanceValue = availableBalObj.toString();
                    }
                }
                
                if (Objects.nonNull(availableBalanceValue) && !availableBalanceValue.trim().isEmpty()) {
                    String numericBalance = extractNumericBalance(availableBalanceValue);
                    String formattedBalance = formatBalanceWithCommas(numericBalance);
                    headerData.put("availableBalance", formattedBalance);
                } else {
                    headerData.put("availableBalance", "0.00");
                }
                
                // Additional fields
                if (Objects.nonNull(accountDetails.getAccountHolderName())) {
                    headerData.put("accountName", accountDetails.getAccountHolderName());
                    String greeting = "Dear " + accountDetails.getAccountHolderName() + " " + customerId;
                    headerData.put("greeting", greeting);
                } else {
                    headerData.put("greeting", "Dear " + customerId);
                }
                headerData.put("subtitle", "Below is an e-Statement of your Account");
                
                if (Objects.nonNull(accountDetails.getBankName())) {
                    headerData.put("branchName", accountDetails.getBankName());
                }
            } else if (Objects.nonNull(rawAccountData)) {
                // Fallback to raw data
                log.warn("Transformed response is empty, extracting directly from raw API response");
                headerData.put("accountNumber", Objects.nonNull(accountNumber) ? accountNumber : "");
                headerData.put("customerNo", Objects.nonNull(customerId) ? customerId : "");
                headerData.put("statementDate", getCurrentDateFormatted());
                headerData.put("statementPeriod", getStatementPeriodFormatted());
                headerData.put("accountType", "CK");
                headerData.put("currency", "QAR");
                
                Object ibanObj = rawAccountData.get("iban");
                if (ibanObj != null) {
                    headerData.put("iban", ibanObj.toString().trim());
                } else {
                    headerData.put("iban", "");
                }
                
                Object currentBalObj = rawAccountData.get("currentBalance");
                if (currentBalObj == null) {
                    currentBalObj = rawAccountData.get("currnetBalance");
                }
                if (currentBalObj != null) {
                    String currentBalanceValue = currentBalObj.toString();
                    String numericBalance = extractNumericBalance(currentBalanceValue);
                    String formattedBalance = formatBalanceWithCommas(numericBalance);
                    headerData.put("currentBalance", formattedBalance);
                } else {
                    headerData.put("currentBalance", "0.00");
                }
                
                Object availableBalObj = rawAccountData.get("availableBalance");
                if (availableBalObj != null) {
                    String availableBalanceValue = availableBalObj.toString();
                    String numericBalance = extractNumericBalance(availableBalanceValue);
                    String formattedBalance = formatBalanceWithCommas(numericBalance);
                    headerData.put("availableBalance", formattedBalance);
                } else {
                    headerData.put("availableBalance", "0.00");
                }
                
                Object accountNameObj = rawAccountData.get("accountHolderName");
                if (Objects.isNull(accountNameObj)) {
                    accountNameObj = rawAccountData.get("accountName");
                }
                if (Objects.nonNull(accountNameObj)) {
                    String accountName = accountNameObj.toString();
                    headerData.put("accountName", accountName);
                    headerData.put("greeting", "Dear " + accountName + " " + customerId);
                } else {
                    headerData.put("greeting", "Dear " + customerId);
                }
                headerData.put("subtitle", "Below is an e-Statement of your Account");
            }
        } catch (Exception e) {
            log.error("Error fetching account details: ", e);
        }
        
        // Set defaults if not set
        if (!headerData.containsKey("statementDate")) {
            headerData.put("statementDate", getCurrentDateFormatted());
        }
        if (!headerData.containsKey("statementPeriod")) {
            headerData.put("statementPeriod", getStatementPeriodFormatted());
        }
        if (!headerData.containsKey("accountType")) {
            headerData.put("accountType", "CK");
        }
        if (!headerData.containsKey("greeting")) {
            headerData.put("greeting", "Dear " + customerId);
        }
        if (!headerData.containsKey("subtitle")) {
            headerData.put("subtitle", "Below is an e-Statement of your Account");
        }
        if (!headerData.containsKey("accountNumber") || Objects.isNull(headerData.get("accountNumber")) || headerData.get("accountNumber").isEmpty()) {
            headerData.put("accountNumber", accountNumber);
        }
        if (!headerData.containsKey("customerNo") || Objects.isNull(headerData.get("customerNo")) || headerData.get("customerNo").isEmpty()) {
            headerData.put("customerNo", customerId);
        }
        if (!headerData.containsKey("currency") || Objects.isNull(headerData.get("currency")) || headerData.get("currency").isEmpty()) {
            headerData.put("currency", "QAR");
        }
        if (!headerData.containsKey("iban") || Objects.isNull(headerData.get("iban")) || headerData.get("iban").isEmpty()) {
            headerData.put("iban", "");
        }
        if (!headerData.containsKey("currentBalance") || Objects.isNull(headerData.get("currentBalance")) || headerData.get("currentBalance").isEmpty()) {
            headerData.put("currentBalance", "0.00");
        }
        if (!headerData.containsKey("availableBalance") || Objects.isNull(headerData.get("availableBalance")) || headerData.get("availableBalance").isEmpty()) {
            headerData.put("availableBalance", "0.00");
        }
        
        log.info("Header data prepared: {}", headerData);
        statementData.setHeaderData(headerData);
        
        // Fetch transactions
        try {
            BaseServiceRequest transactionRequest = createTransactionRequest(accountNumber);
            ApiResponse<Map<String, Object>> transactionResponse = processUIRequest.processEndToEndFlow(
                    "TRANSACTION.STATEMENT", transactionRequest, null);
            
            ApiResponse<Map<String, Object>> processedResponse = accountTransactionService.postProcessAccountTransactions(
                    transactionResponse, transactionRequest);
            
            List<StatementData.Transaction> transactionList = new ArrayList<>();
            
            if (Objects.nonNull(processedResponse) && Objects.nonNull(processedResponse.getData())) {
                List<Map<String, Object>> transactions = processedResponse.getData();
                
                for (Map<String, Object> transactionMap : transactions) {
                    StatementData.Transaction transaction = new StatementData.Transaction();
                    
                    String dateValue = getStringValue(transactionMap, "transactionDate", "valueDate", "date");
                    if (Objects.isNull(dateValue) || dateValue.isEmpty()) {
                        String postingDate = getStringValue(transactionMap, "postingDate");
                        if (Objects.nonNull(postingDate) && !postingDate.isEmpty()) {
                            if (postingDate.contains("T")) {
                                dateValue = postingDate.substring(0, postingDate.indexOf("T"));
                            } else {
                                dateValue = postingDate;
                            }
                        }
                    }
                    transaction.setDate(dateValue);
                    transaction.setDescription(getStringValue(transactionMap, "transactionDesc", "remarks", "trxRemarks", "explanation", "description"));
                    transaction.setReference(getStringValue(transactionMap, "originTracerNumber", "traSeqNum1", "traSeqNum2", "docNum", "transactionReferenceNo", "reference"));
                    
                    String transactionType = getStringValue(transactionMap, "transactionType", "debitCreditInd");
                    String transactionAmount = getStringValue(transactionMap, "transactionAmount", "equTransactionAmount");
                    String debitAmount = getStringValue(transactionMap, "debitAmount", "equDebitAmount");
                    String creditAmount = getStringValue(transactionMap, "creditAmount", "equCreditAmount");
                    
                    if ("DR".equalsIgnoreCase(transactionType) || "D".equalsIgnoreCase(transactionType)) {
                        if (Objects.nonNull(transactionAmount) && !transactionAmount.isEmpty() && !transactionAmount.equals("0")) {
                            transaction.setDebit(formatAmount(transactionAmount));
                        } else if (Objects.nonNull(debitAmount) && !debitAmount.isEmpty() && !debitAmount.equals("0")) {
                            transaction.setDebit(formatAmount(debitAmount));
                        } else {
                            transaction.setDebit(null);
                        }
                        transaction.setCredit(null);
                    } else if ("CR".equalsIgnoreCase(transactionType) || "C".equalsIgnoreCase(transactionType)) {
                        if (Objects.nonNull(transactionAmount) && !transactionAmount.isEmpty() && !transactionAmount.equals("0")) {
                            transaction.setCredit(formatAmount(transactionAmount));
                        } else if (Objects.nonNull(creditAmount) && !creditAmount.isEmpty() && !creditAmount.equals("0")) {
                            transaction.setCredit(formatAmount(creditAmount));
                        } else {
                            transaction.setCredit(null);
                        }
                        transaction.setDebit(null);
                    } else {
                        if (debitAmount != null && !debitAmount.isEmpty() && !debitAmount.equals("0")) {
                            transaction.setDebit(formatAmount(debitAmount));
                        } else {
                            transaction.setDebit(null);
                        }
                        if (creditAmount != null && !creditAmount.isEmpty() && !creditAmount.equals("0")) {
                            transaction.setCredit(formatAmount(creditAmount));
                        } else {
                            transaction.setCredit(null);
                        }
                    }
                    
                    transaction.setBalance(formatAmount(getStringValue(transactionMap, "curBalance", "balance", "currentBalance")));
                    transactionList.add(transaction);
                }
            }
            
            statementData.setTransactions(transactionList);
            log.info("Successfully fetched {} transactions", transactionList.size());
            
        } catch (Exception e) {
            log.error("Error fetching transactions: ", e);
            statementData.setTransactions(new ArrayList<>());
        }
        
        // Set footer
        StatementData.StatementFooter footer = new StatementData.StatementFooter();
        footer.setGeneratedOn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        footer.setDisclaimer("This is a computer-generated statement. No signature is required.");
        footer.setContactInfo("For queries, contact: support@bank.com");
        statementData.setFooter(footer);
        
        return statementData;
    }
    
    private BaseServiceRequest createAccountDetailsRequest(String accountNumber, String customerId) {
        BaseServiceRequest request = new BaseServiceRequest();
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("acctNumber", accountNumber);
        requestInfo.put("customerId", customerId);
        request.setRequestInfo(requestInfo);
        return request;
    }
    
    private BaseServiceRequest createTransactionRequest(String accountNumber) {
        BaseServiceRequest request = new BaseServiceRequest();
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("acctNumber", accountNumber);
        requestInfo.put("transactionType", "any");
        request.setRequestInfo(requestInfo);
        return request;
    }
    
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && Objects.nonNull(map.get(key))) {
                return String.valueOf(map.get(key));
            }
        }
        return null;
    }
    
    private String formatAmount(String amount) {
        if (Objects.isNull(amount) || amount.trim().isEmpty()) {
            return "0.00";
        }
        try {
            String cleanAmount = amount.replaceAll("[^0-9.\\-]", "");
            if (cleanAmount.isEmpty()) {
                return "0.00";
            }
            double value = Double.parseDouble(cleanAmount);
            return String.format("%.2f", value);
        } catch (Exception e) {
            return amount;
        }
    }
    
    private String extractNumericBalance(String balanceString) {
        if (Objects.isNull(balanceString) || balanceString.trim().isEmpty()) {
            return "0.00";
        }
        try {
            String[] parts = balanceString.trim().split("\\s+");
            if (parts.length > 0) {
                String numericPart = parts[0].replaceAll("[^0-9.\\-]", "");
                if (numericPart.isEmpty()) {
                    return "0.00";
                }
                Double.parseDouble(numericPart);
                return numericPart;
            }
            return "0.00";
        } catch (Exception e) {
            log.warn("Error extracting numeric balance from: {}", balanceString, e);
            return "0.00";
        }
    }
    
    private String formatBalanceWithCommas(String balance) {
        if (Objects.isNull(balance) || balance.trim().isEmpty()) {
            return "0.00";
        }
        try {
            String cleanBalance = balance.replace(",", "");
            double value = Double.parseDouble(cleanBalance);
            return String.format("%,.2f", value);
        } catch (Exception e) {
            log.warn("Error formatting balance: {}", balance, e);
            return balance;
        }
    }
    
    private String getCurrentDateFormatted() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    private String getStatementPeriodFormatted() {
        LocalDate currentDate = LocalDate.now();
        LocalDate sixMonthsBack = currentDate.minus(6, ChronoUnit.MONTHS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return sixMonthsBack.format(formatter) + " to " + currentDate.format(formatter);
    }
}
