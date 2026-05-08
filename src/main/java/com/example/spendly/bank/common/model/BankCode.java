package com.example.spendly.bank.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BankCode {
    MONO("MonoBank"),
    RABO("RaboBank"),
    PRIVAT("PrivatBank");

    final String bankName;

    public BankCode getBankCode(String bankName) {
        for (BankCode bankCode : BankCode.values()) {
            if (bankCode.bankName.equalsIgnoreCase(bankName)) {
                return bankCode;
            }
        }
        return null;
    }
}
