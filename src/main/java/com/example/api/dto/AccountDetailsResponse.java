package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsResponse {
    private String availableBalance;
    private String currentBalance;
    private String currencyCode;
    private String holdBal;
    private String accountNickname;
    private String iban;
    private String accountHolderName;
    private String bankName;
    private String swiftCode;
    private String postBoxNumber;
    private String branchName;
    private String fullAddress;
    private String country;
    private String city;
}

