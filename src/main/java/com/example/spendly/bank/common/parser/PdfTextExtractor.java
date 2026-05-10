package com.example.spendly.bank.common.source;

import com.example.spendly.bank.common.model.BankCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class PdfTextExtractor {

    public TextStatementSource extract(File file, BankCode bankCode) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);

            return new TextStatementSource(text);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot extract text from " + bankCode.getBankName() + " PDF statement: " + file.getName(),
                    e
            );
        }
    }
}