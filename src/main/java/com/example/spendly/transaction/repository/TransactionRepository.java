package com.example.spendly.transaction.repository;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionDateBetween(
            LocalDate from,
            LocalDate to
    );

    List<Transaction> findByTransactionDateBetweenAndBankCodeIn(
            LocalDate from,
            LocalDate to,
            List<BankCode> bankCodes
    );
}