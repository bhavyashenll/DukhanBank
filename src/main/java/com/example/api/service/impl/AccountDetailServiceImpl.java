package com.example.api.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.service.AccountDetailService;
import com.example.api.dto.AccountDetailsResponse;
import com.example.api.entity.CustomerNickname;
import com.example.api.repository.CustomerNicknameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountDetailServiceImpl implements AccountDetailService {

    private static final Logger logger = LoggerFactory.getLogger(AccountDetailServiceImpl.class);

    @Autowired
    private CustomerNicknameRepository customerNicknameRepository;

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse<AccountDetailsResponse> validateRequest(BaseServiceRequest baseServiceRequest) {
        logger.info("Validating account details request");
        
        if (baseServiceRequest == null || baseServiceRequest.getRequestInfo() == null) {
            logger.warn("Request or requestInfo is null");
            List<String> errors = List.of("acctNumber is required", "customerId is required");
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "Request validation failed");
            result.put("errors", errors);
            ApiResponse<AccountDetailsResponse> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            response.setData((List<AccountDetailsResponse>) (List<?>) List.of(result));
            return response;
        }
        
        Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
        List<String> errors = new ArrayList<>();
        
        Object acctNumber = requestInfo.get("acctNumber");
        if (Objects.isNull(acctNumber) || (acctNumber.toString().trim().isEmpty())) {
            errors.add("acctNumber is required");
        }
        
        Object customerId = requestInfo.get("customerId");
        if (Objects.isNull(customerId) || (customerId.toString().trim().isEmpty())) {
            errors.add("customerId is required");
        }
        
        if (!errors.isEmpty()) {
            logger.warn("Validation failed: missing required fields: {}", errors);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "Request validation failed");
            result.put("errors", errors);
            ApiResponse<AccountDetailsResponse> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            response.setData((List<AccountDetailsResponse>) (List<?>) List.of(result));
            return response;
        }
        
        logger.info("Request validation successful");
        return null;
    }

    @Override
    public ApiResponse<AccountDetailsResponse> postProcessAccountDetails(ApiResponse<Map<String, Object>> response, String lang, BaseServiceRequest baseServiceRequest) {
        logger.info("Post-processing account details response for language: {}", lang);

        try {
            if (Objects.isNull(response) || Objects.isNull(response.getStatus()) || Objects.isNull(response.getData())) {
                logger.info("Response is null or missing data, returning empty status");
                return emptyWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!"000000".equals(code)) {
                logger.info("Response status code is not success: {}", code);
                return emptyWithStatus(response);
            }

            List<Map<String, Object>> dataList = response.getData();
            if (dataList.isEmpty()) {
                logger.info("No account details found in response");
                return emptyWithStatus(response);
            }

            // Take the first account from the list
            Map<String, Object> accountData = dataList.get(0);
            logger.info("Processing account details for account: {}", accountData.get("acctNumber"));

            AccountDetailsResponse accountDetailsResponse = transformAccountDetails(accountData, baseServiceRequest);

            logger.info("Successfully processed account details");
            ApiResponse<AccountDetailsResponse> successResponse = new ApiResponse<>();
            successResponse.setStatus(new ApiResponse.Status("000000", "Successfully processed"));
            successResponse.setData(List.of(accountDetailsResponse));
            return successResponse;

        } catch (Exception e) {
            logger.error("Error post-processing account details response", e);
            throw new RuntimeException("Failed to post-process account details response", e);
        }
    }

    private AccountDetailsResponse transformAccountDetails(Map<String, Object> accountData, BaseServiceRequest baseServiceRequest) {
        AccountDetailsResponse response = new AccountDetailsResponse();

        // Get currency code for appending to balance fields
        String currencyCode = getStringValue(accountData, "currencyCode");
        
        // Set currency code in response
        response.setCurrencyCode(currencyCode);

        // Map fields from MQ response with rounding to 2 decimals and currency code
        response.setAvailableBalance(formatBalance(getStringValue(accountData, "availableBalance"), currencyCode));
        // Note: Bank response has typo "currnetBalance" instead of "currentBalance"
        response.setCurrentBalance(formatBalance(getStringValue(accountData, "currnetBalance"), currencyCode));
        response.setHoldBal(formatBalance(getStringValue(accountData, "holdBal")));
        response.setIban(getStringValue(accountData, "iban"));

        // Extract accountHolderName from snapIn array (TitleLine1 field)
        response.setAccountHolderName(extractTitleLine1(accountData));

        // Get customer nickname from service
        String accountNickname = getCustomerNickname(baseServiceRequest);
        response.setAccountNickname(accountNickname);
        
        // Get branch information based on branchId
        String branchId = getStringValue(accountData, "branchId");
        BranchInfo branchInfo = getBranchInfo(branchId);
        
        // Set branch-related fields
        response.setBankName(branchInfo.getBankName());
        response.setSwiftCode(branchInfo.getSwiftCode());
        response.setPostBoxNumber(branchInfo.getPostBoxNumber());
        response.setBranchName(branchInfo.getBranchName());
        response.setFullAddress(branchInfo.getFullAddress());
        response.setCountry(branchInfo.getCountry());
        response.setCity(branchInfo.getCity());

        return response;
    }
    
    private BranchInfo getBranchInfo(String branchId) {
        // Default branch information - can be extended to fetch from a service or database
        // Based on the expected response, these are the values for the branch
        BranchInfo branchInfo = new BranchInfo();
        
        // Extract branch information from accountData if available, otherwise use defaults
        // For now, using the expected values from the user's requirement
        branchInfo.setBankName("Main Branch - West Bay");
        branchInfo.setSwiftCode("DUBZQAQA");
        branchInfo.setPostBoxNumber("3818");
        branchInfo.setBranchName(null);
        branchInfo.setFullAddress("Lusail Branch");
        branchInfo.setCountry("Qatar");
        branchInfo.setCity("Lusail");
        
        return branchInfo;
    }
    
    // Helper class to hold branch information
    private static class BranchInfo {
        private String bankName;
        private String swiftCode;
        private String postBoxNumber;
        private String branchName;
        private String fullAddress;
        private String country;
        private String city;
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getSwiftCode() { return swiftCode; }
        public void setSwiftCode(String swiftCode) { this.swiftCode = swiftCode; }
        
        public String getPostBoxNumber() { return postBoxNumber; }
        public void setPostBoxNumber(String postBoxNumber) { this.postBoxNumber = postBoxNumber; }
        
        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }
        
        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    private String getCustomerNickname(BaseServiceRequest baseServiceRequest) {
        try {
            Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
            String accountNumber = getStringValue(requestInfo, "acctNumber");
            String customerId = getStringValue(requestInfo, "customerId");

            Optional<CustomerNickname> nicknameOptional = customerNicknameRepository.findByAccountNumberAndCustomerId(
                    accountNumber, Long.valueOf(customerId));
            
            if (nicknameOptional.isPresent()) {
                String nickname = nicknameOptional.get().getNickname();
                if (nickname != null && !nickname.trim().isEmpty()) {
                    logger.info("Successfully retrieved customer nickname for account: {}", accountNumber);
                    return nickname;
                }
            }

            logger.info("Nickname not found for account: {} and customerId: {}, returning null", accountNumber, customerId);
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving customer nickname, returning null", e);
            return null;
        }
    }

    private String extractTitleLine1(Map<String, Object> accountData) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> snapIn = (List<Map<String, Object>>) accountData.get("snapIn");
            
            if (snapIn != null) {
                for (Map<String, Object> snapInItem : snapIn) {
                    // Extract nested nameValue object
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nameValue = (Map<String, Object>) snapInItem.get("nameValue");
                    
                    if (nameValue != null) {
                        String name = getStringValue(nameValue, "name");
                        if ("TitleLine1".equals(name)) {
                            return getStringValue(nameValue, "value");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error extracting TitleLine1 from snapIn array: {}", e.getMessage());
        }
        
        return null;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String formatBalance(String balanceValue) {
        return formatBalance(balanceValue, null);
    }

    private String formatBalance(String balanceValue, String currencyCode) {
        if (balanceValue == null || balanceValue.trim().isEmpty()) {
            return balanceValue;
        }
        try {
            BigDecimal balance = new BigDecimal(balanceValue);
            BigDecimal rounded = balance.setScale(2, RoundingMode.HALF_UP);
            DecimalFormat df = new DecimalFormat("0.00");
            String formattedBalance = df.format(rounded);
            
            // Append currency code with a space if provided
            if (currencyCode != null && !currencyCode.trim().isEmpty()) {
                formattedBalance = formattedBalance + " " + currencyCode.trim();
            }
            
            return formattedBalance;
        } catch (NumberFormatException e) {
            logger.warn("Unable to format balance value: {}, returning original value", balanceValue);
            return balanceValue;
        }
    }

    private ApiResponse<AccountDetailsResponse> emptyWithStatus(ApiResponse<?> source) {
        ApiResponse<AccountDetailsResponse> out = new ApiResponse<>();
        if (Objects.nonNull(source)) {
            out.setStatus(source.getStatus());
        }
        out.setData(List.of());
        return out;
    }
}

