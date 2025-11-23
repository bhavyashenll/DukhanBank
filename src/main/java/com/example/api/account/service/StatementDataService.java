package com.example.api.account.service;

import com.example.api.account.domain.dto.StatementData;

public interface StatementDataService {
    StatementData fetchStatementData(String accountNumber, String customerId, 
                                     String serviceId, String moduleId, String subModuleId,
                                     String screenId, String channel, String lang);
}

