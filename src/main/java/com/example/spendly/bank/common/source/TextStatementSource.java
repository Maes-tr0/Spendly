package com.example.spendly.bank.common.source;

/**
 * Statement source based on extracted plain text.
 * <p>
 * Usually used for PDF statements after text extraction.
 */
public record TextStatementSource(
        String text
) implements StatementSource {
}