package com.bank.retail.api.dto;

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
    private String curNameEN;
    private String shortCurNameEN;
    private String curNameAR;
    private String shortCurNameAR;
    private String ttBuy;
    private Object ttSell;
}


