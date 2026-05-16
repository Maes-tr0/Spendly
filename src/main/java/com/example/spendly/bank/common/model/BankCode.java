package com.example.spendly.bank.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BankCode {
    MONO("MonoBank"),
    RABO("RaboBank"),
    PRIVAT("PrivatBank");

    final String bankName;

    public static BankCode fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bank code cannot be empty");
        }

        return Arrays.stream(values())
                .filter(bank -> bank.isSame(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported bank code: " + value));
    }

    public String getAlphabeticCode() {
        return name();
    }

    public boolean isSame(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return getAlphabeticCode().equalsIgnoreCase(value.trim());
    }

    public static boolean isSupported(BankCode bankCode) {
        if (bankCode == null) {
            return false;
        }

        return Arrays.asList(BankCode.values()).contains(bankCode);
    }
}
