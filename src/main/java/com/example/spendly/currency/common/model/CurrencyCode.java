package com.example.spendly.currency.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum CurrencyCode {

    EUR(978),
    USD(840),
    UAH(980),
    GBP(826),
    INR(356),
    THB(764);

    private final int numericCode;

    public String getAlphabeticCode() {
        return name();
    }

    public boolean isSame(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return getAlphabeticCode().equalsIgnoreCase(value.trim());
    }

    public static CurrencyCode fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be empty");
        }

        return Arrays.stream(values())
                .filter(currency -> currency.isSame(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported currency code: " + value));
    }
}