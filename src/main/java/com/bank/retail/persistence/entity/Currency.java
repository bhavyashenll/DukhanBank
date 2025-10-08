package com.bank.retail.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}


