package com.example.spendly.transaction.service;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.transaction.model.ConvertedTransaction;
import com.example.spendly.transaction.model.Transaction;
import com.example.spendly.transaction.model.TransactionCalculationResult;
import com.example.spendly.transaction.model.request.TransactionAnalysisRequest;
import com.example.spendly.transaction.model.response.TransactionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionAnalysisService {

    private final TransactionQueryService transactionQueryService;
    private final TransactionAmountConverter transactionAmountConverter;
    private final TransactionCalculator transactionCalculator;
    private final TransactionAnalysisResponseAssembler transactionAnalysisResponseAssembler;
    private final TransactionAnalysisValidator transactionAnalysisValidator;

    public TransactionAnalysisResponse analyze(TransactionAnalysisRequest request) {
        List<BankCode> bankCodes = resolveBankCodes(request.bankCodes());
        CurrencyCode targetCurrency = resolveTargetCurrency(request.targetCurrency());

        LocalDate from = resolveDate(request.dateFrom());
        LocalDate to = resolveDate(request.dateTo());

        transactionAnalysisValidator.validateDateRange(from, to);

        List<Transaction> transactions = transactionQueryService.findTransactions(
                from,
                to,
                bankCodes
        );

        List<ConvertedTransaction> convertedTransactions =
                transactionAmountConverter.convertAll(transactions, targetCurrency);

        TransactionCalculationResult calculationResult =
                transactionCalculator.calculate(convertedTransactions);

        return transactionAnalysisResponseAssembler.assemble(
                calculationResult,
                targetCurrency,
                from,
                to
        );
    }

    private List<BankCode> resolveBankCodes(List<String> requestedBankCodes) {
        if (requestedBankCodes == null || requestedBankCodes.isEmpty()) {
            return List.of();
        }

        List<String> cleanedBankCodes = requestedBankCodes.stream()
                .filter(bankCode -> bankCode != null && !bankCode.isBlank())
                .map(String::trim)
                .toList();

        if (cleanedBankCodes.isEmpty()) {
            throw new IllegalArgumentException("Bank codes cannot contain only null or blank values");
        }

        return cleanedBankCodes.stream()
                .map(BankCode::fromString)
                .distinct()
                .toList();
    }

    private CurrencyCode resolveTargetCurrency(String targetCurrency) {
        if (targetCurrency == null || targetCurrency.isBlank()) {
            return CurrencyCode.EUR;
        }

        return CurrencyCode.fromString(targetCurrency);
    }

    private LocalDate resolveDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("Date cannot be null or blank");
        }

        return LocalDate.parse(date);
    }
}