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
}
