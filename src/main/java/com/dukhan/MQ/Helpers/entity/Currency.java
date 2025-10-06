package com.dukhan.MQ.Helpers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "iso_code")
    private String isoCode;

    @Column(name = "iso_number")
    private String isoNumber;

    @Column(name = "country")
    private String country;

    @Column(name = "english_name")
    private String englishName;

    @Column(name = "arabic_name")
    private String arabicName;

    @Column(name = "english_short_name")
    private String englishShortName;

    @Column(name = "arabic_short_name")
    private String arabicShortName;

    @Column(name = "status")
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoNumber() {
        return isoNumber;
    }

    public void setIsoNumber(String isoNumber) {
        this.isoNumber = isoNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getArabicName() {
        return arabicName;
    }

    public void setArabicName(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getEnglishShortName() {
        return englishShortName;
    }

    public void setEnglishShortName(String englishShortName) {
        this.englishShortName = englishShortName;
    }

    public String getArabicShortName() {
        return arabicShortName;
    }

    public void setArabicShortName(String arabicShortName) {
        this.arabicShortName = arabicShortName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


