package com.example.api.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementData {
    private Map<String, String> headerData;
    private List<Transaction> transactions;
    private StatementFooter footer;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        private String date;
        private String description;
        private String reference;
        private String debit;
        private String credit;
        private String balance;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatementFooter {
        private String generatedOn;
        private String disclaimer;
        private String contactInfo;
    }
}

