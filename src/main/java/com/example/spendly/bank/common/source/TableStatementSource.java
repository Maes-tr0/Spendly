package com.example.spendly.bank.common.source;

import java.util.List;

/**
 * Statement source based on table rows.
 * <p>
 * Can be used for XLSX and CSV statements after converting cells to strings.
 */
public record TableStatementSource(
        List<List<String>> rows
) implements StatementSource {

    public String getValue(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return "";
        }

        List<String> row = rows.get(rowIndex);

        if (columnIndex < 0 || columnIndex >= row.size()) {
            return "";
        }

        return row.get(columnIndex);
    }

    public int rowCount() {
        return rows.size();
    }
}