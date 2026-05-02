package com.example.spendly.transaction.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum CurrencyCode {
    EUR("EUR"),
    USD("USD"),
    UAH("UAH"),
    GBP("GBP"),
    INR("INR"),
    THB("THB");

    private final String code;

    public static CurrencyCode fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(currency -> currency.isSame(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported currency code: " + value));
    }

    public boolean isSame(String value) {
        if (value == null) {
            return false;
        }

        return code.equalsIgnoreCase(value.trim());
    }
}
