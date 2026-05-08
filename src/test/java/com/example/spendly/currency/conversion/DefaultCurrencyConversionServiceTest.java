package com.example.spendly.currency.conversion;

import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.currency.rate.ExchangeRateService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCurrencyConversionServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private DefaultCurrencyConversionService currencyConversionService;

    @Nested
    class ConvertTests {

        @Test
        void givenAmountAndExchangeRate_whenConvert_thenReturnConvertedAmount() {
            CurrencyCode from = CurrencyCode.USD;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("10.00");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            when(exchangeRateService.getRate(from, to, transactionDate))
                    .thenReturn(new BigDecimal("0.86"));

            BigDecimal result = currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            );

            assertThat(result).isEqualByComparingTo("8.60");

            verify(exchangeRateService).getRate(from, to, transactionDate);
        }

        @Test
        void givenAmountAndLongExchangeRate_whenConvert_thenReturnRoundedAmount() {
            CurrencyCode from = CurrencyCode.USD;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("10.00");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            when(exchangeRateService.getRate(from, to, transactionDate))
                    .thenReturn(new BigDecimal("0.86789"));

            BigDecimal result = currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            );

            assertThat(result).isEqualByComparingTo("8.68");

            verify(exchangeRateService).getRate(from, to, transactionDate);
        }

        @Test
        void givenVerySmallConvertedAmount_whenConvert_thenReturnZeroAmount() {
            CurrencyCode from = CurrencyCode.UAH;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("0.01");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            when(exchangeRateService.getRate(from, to, transactionDate))
                    .thenReturn(new BigDecimal("0.0001"));

            BigDecimal result = currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            );

            assertThat(result).isEqualByComparingTo("0.00");

            verify(exchangeRateService).getRate(from, to, transactionDate);
        }

        @Test
        void givenSameCurrency_whenConvert_thenReturnSameAmountAndDoNotCallExchangeRateService() {
            CurrencyCode from = CurrencyCode.EUR;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("25.00");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            BigDecimal result = currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            );

            assertThat(result).isEqualByComparingTo("25.00");

            verifyNoInteractions(exchangeRateService);
        }

        @Test
        void givenSameCurrencyAndAmountWithMoreDecimals_whenConvert_thenReturnRoundedSameAmount() {
            CurrencyCode from = CurrencyCode.EUR;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("25.567");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            BigDecimal result = currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            );

            assertThat(result).isEqualByComparingTo("25.57");

            verifyNoInteractions(exchangeRateService);
        }

        @Test
        void givenNullAmount_whenConvert_thenThrowIllegalArgumentException() {
            CurrencyCode from = CurrencyCode.USD;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = null;
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            assertThatThrownBy(() -> currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount cannot be null");

            verifyNoInteractions(exchangeRateService);
        }

        @Test
        void givenNullFromCurrency_whenConvert_thenThrowIllegalArgumentException() {
            CurrencyCode from = null;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("10.00");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            assertThatThrownBy(() -> currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency cannot be null");

            verifyNoInteractions(exchangeRateService);
        }

        @Test
        void givenNullToCurrency_whenConvert_thenThrowIllegalArgumentException() {
            CurrencyCode from = CurrencyCode.USD;
            CurrencyCode to = null;
            BigDecimal amount = new BigDecimal("10.00");
            LocalDate transactionDate = LocalDate.of(2025, 7, 15);

            assertThatThrownBy(() -> currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency cannot be null");

            verifyNoInteractions(exchangeRateService);
        }

        @Test
        void givenNullTransactionDate_whenConvert_thenThrowIllegalArgumentException() {
            CurrencyCode from = CurrencyCode.USD;
            CurrencyCode to = CurrencyCode.EUR;
            BigDecimal amount = new BigDecimal("10.00");
            LocalDate transactionDate = null;

            assertThatThrownBy(() -> currencyConversionService.convert(
                    from,
                    to,
                    amount,
                    transactionDate
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Transaction date cannot be null");

            verifyNoInteractions(exchangeRateService);
        }
    }
}