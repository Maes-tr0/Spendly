package com.example.spendly.transaction.service;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.transaction.model.Transaction;
import com.example.spendly.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final TransactionAnalysisValidator  transactionAnalysisValidator;

    public List<Transaction> findTransactions(
            LocalDate from,
            LocalDate to,
            List<BankCode> bankCodes
    ) {

        transactionAnalysisValidator.validateDateRange(from, to);

        if(bankCodes == null || bankCodes.isEmpty()){
            return transactionRepository.findByTransactionDateBetween(from, to);
        }

        if(!bankCodes.stream().allMatch(BankCode::isSupported))
        {
            throw new IllegalArgumentException("One of bank code is not supported");
        }

        return transactionRepository.findByTransactionDateBetweenAndBankCodeIn(from, to, bankCodes);
    }

}
