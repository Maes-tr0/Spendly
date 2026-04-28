package com.example.spendly.transaction.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Transaction {

    @Id
    private Long id;
}
