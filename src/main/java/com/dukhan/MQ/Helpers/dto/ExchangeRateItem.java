package com.dukhan.MQ.Helpers.dto;

public class ExchangeRateItem {
    private String isoCode;
    private String isoCodeNum;
    private String curNameEN;
    private String shortCurNameEN;
    private String curNameAR;
    private String shortCurNameAR;
    private String ttBuy;
    private Object ttSell;

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoCodeNum() {
        return isoCodeNum;
    }

    public void setIsoCodeNum(String isoCodeNum) {
        this.isoCodeNum = isoCodeNum;
    }

    public String getCurNameEN() {
        return curNameEN;
    }

    public void setCurNameEN(String curNameEN) {
        this.curNameEN = curNameEN;
    }

    public String getShortCurNameEN() {
        return shortCurNameEN;
    }

    public void setShortCurNameEN(String shortCurNameEN) {
        this.shortCurNameEN = shortCurNameEN;
    }

    public String getCurNameAR() {
        return curNameAR;
    }

    public void setCurNameAR(String curNameAR) {
        this.curNameAR = curNameAR;
    }

    public String getShortCurNameAR() {
        return shortCurNameAR;
    }

    public void setShortCurNameAR(String shortCurNameAR) {
        this.shortCurNameAR = shortCurNameAR;
    }

    public String getTtBuy() {
        return ttBuy;
    }

    public void setTtBuy(String ttBuy) {
        this.ttBuy = ttBuy;
    }

    public Object getTtSell() {
        return ttSell;
    }

    public void setTtSell(Object ttSell) {
        this.ttSell = ttSell;
    }
}


