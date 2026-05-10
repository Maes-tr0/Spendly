package com.example.spendly.transaction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Transaction {

    @Id
    private Long id;


    /*
        Виписки можна додавати терміном більше місяця для коректного підрахунку доходу та витрат,
        також ми маємо сума до транзакції й ми можемо її використовувати




        ID
        дата транзакції
        сума транзакції
        валюта транзакції
        назва банку


     */
}
