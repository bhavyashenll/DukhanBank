package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateItem {
    private String isoCode;
    private String isoCodeNum;
    private String curName; // based on lang in header
    private String shortCurName; // based on lang in header
    private String ttBuy;
    private String ttSell;
}
