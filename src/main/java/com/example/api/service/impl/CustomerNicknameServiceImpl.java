package com.example.api.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.DeviceInfoDto;
import com.example.api.entity.CustomerNickname;
import com.example.api.repository.CustomerNicknameRepository;
import com.example.api.service.CustomerNicknameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerNicknameServiceImpl implements CustomerNicknameService {

    private final CustomerNicknameRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Object> getCustomerNickname(BaseServiceRequest request) {
        log.info("Processing get customer nickname request");
        
        try {
            validateRequest(request);
            validateDeviceInfo(request.getDeviceInfo());

            Map<String, Object> requestInfo = request.getRequestInfo();
            String accountNumber = getStringValue(requestInfo, "accountNumber");
            String customerId = getStringValue(requestInfo, "customerId");

            if (accountNumber == null || customerId == null) {
                log.warn("Missing required fields: accountNumber={}, customerId={}", accountNumber, customerId);
                return ApiResponse.badRequest();
            }

            // Find record by accountNumber and customerId
            Optional<CustomerNickname> data = repository.findByAccountNumberAndCustomerId(
                    accountNumber, Long.valueOf(customerId));
            if (data.isEmpty()) {
                log.warn("No customer nickname found for accountNumber: {} and customerId: {}", accountNumber, customerId);
                return ApiResponse.noDataFound();
            }

            CustomerNickname record = data.get();

            // Create response object with nickname
            Map<String, Object> nicknameResponse = new HashMap<>();
            nicknameResponse.put("nickname", record.getNickname());

            log.info("Successfully retrieved customer nickname for accountNumber: {}", accountNumber);
            return ApiResponse.success(List.of(nicknameResponse));

        } catch (Exception e) {
            log.error("Error processing get customer nickname request", e);
            return ApiResponse.error();
        }
    }

    @Override
    @Transactional
    public ApiResponse<Object> upsertCustomerNickname(BaseServiceRequest request) {
        log.info("Processing upsert customer nickname request");
        
        try {
            validateRequest(request);
            validateDeviceInfo(request.getDeviceInfo());

            Map<String, Object> requestInfo = request.getRequestInfo();
            String accountNumber = getStringValue(requestInfo, "accountNumber");
            String customerId = getStringValue(requestInfo, "customerId");
            String nickname = getStringValue(requestInfo, "nickname");

            if (accountNumber == null || customerId == null || nickname == null) {
                log.warn("Missing required fields: accountNumber={}, customerId={}, nickname={}", 
                        accountNumber, customerId, nickname);
                return ApiResponse.badRequest();
            }

            // Check if the account already exists with matching customerId
            Optional<CustomerNickname> optionalRecord = repository.findByAccountNumberAndCustomerId(
                    accountNumber, Long.valueOf(customerId));

            if (optionalRecord.isPresent()) {
                CustomerNickname existingRecord = optionalRecord.get();
                // Exists and matches → Update nickname
                existingRecord.setNickname(nickname);
                repository.save(existingRecord);
                log.info("Updated customer nickname for accountNumber: {} and customerId: {}", accountNumber, customerId);
            } else {
                // New account → Create new record
                CustomerNickname newRecord = CustomerNickname.builder()
                        .accountNumber(accountNumber)
                        .customerId(Long.valueOf(customerId))
                        .nickname(nickname)
                        .build();
                repository.save(newRecord);
                log.info("Created new customer nickname for accountNumber: {}", accountNumber);
            }

            return ApiResponse.success(List.of());

        } catch (Exception e) {
            log.error("Error processing upsert customer nickname request", e);
            return ApiResponse.error();
        }
    }

    @Override
    @Transactional
    public ApiResponse<Object> deleteCustomerNickname(BaseServiceRequest request) {
        log.info("Processing delete customer nickname request");
        
        try {
            validateRequest(request);
            validateDeviceInfo(request.getDeviceInfo());

            Map<String, Object> requestInfo = request.getRequestInfo();
            String accountNumber = getStringValue(requestInfo, "accountNumber");

            if (accountNumber == null) {
                log.warn("Missing required field: accountNumber");
                return ApiResponse.badRequest();
            }

            Optional<CustomerNickname> existing = repository.findByAccountNumber(accountNumber);
            if (existing.isEmpty()) {
                log.warn("No customer nickname found to delete for accountNumber: {}", accountNumber);
                return ApiResponse.noDataFound();
            }

            repository.deleteByAccountNumber(accountNumber);
            log.info("Successfully deleted customer nickname for accountNumber: {}", accountNumber);
            
            return ApiResponse.success(List.of());

        } catch (Exception e) {
            log.error("Error processing delete customer nickname request", e);
            return ApiResponse.error();
        }
    }

    // Checks if request and deviceInfo are present
    private void validateRequest(BaseServiceRequest request) {
        if (request == null || request.getRequestInfo() == null) {
            throw new IllegalArgumentException("Request or requestInfo is null");
        }
    }

    // Checks if all required device info keys exist and are non-null
    private void validateDeviceInfo(DeviceInfoDto deviceInfo) {
        if (deviceInfo == null) {
            throw new IllegalArgumentException("DeviceInfo is null");
        }
        
        try {
            if (deviceInfo.getDeviceId() == null ||
                    deviceInfo.getIpAddress() == null ||
                    deviceInfo.getVendorId() == null ||
                    deviceInfo.getOsVersion() == null ||
                    deviceInfo.getAppVersion() == null ||
                    deviceInfo.getEndToEndId() == null) {
                throw new IllegalArgumentException("DeviceInfo validation failed: missing required fields");
            }
        } catch (Exception e) {
            log.error("Error validating deviceInfo", e);
            throw new IllegalArgumentException("DeviceInfo validation failed", e);
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
