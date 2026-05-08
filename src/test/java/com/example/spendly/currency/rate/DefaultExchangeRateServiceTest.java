package com.example.spendly.currency.rate;

import com.example.spendly.currency.common.model.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DefaultExchangeRateServiceTest {

    private MockRestServiceServer server;
    private DefaultExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@");

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        exchangeRateService = new DefaultExchangeRateService(restClient);
    }

    @Nested
    class GetRateSuccessTests {

        @Test
        void givenValidUsdToEurResponse_whenGetRate_thenReturnExchangeRate() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            server.expect(requestTo(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2025-07-15/v1/currencies/usd.min.json"
            )).andRespond(withSuccess("""
                    {
                      "date": "2025-07-15",
                      "usd": {
                        "eur": 0.86,
                        "uah": 41.50,
                        "gbp": 0.74
                      }
                    }
                    """, MediaType.APPLICATION_JSON));

            BigDecimal result = exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            );

            assertThat(result).isEqualByComparingTo("0.86");

            server.verify();
        }

        @Test
        void givenValidUahToEurResponse_whenGetRate_thenReturnExchangeRate() {
            CurrencyCode fromCurrency = CurrencyCode.UAH;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            server.expect(requestTo(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2025-07-15/v1/currencies/uah.min.json"
            )).andRespond(withSuccess("""
                    {
                      "date": "2025-07-15",
                      "uah": {
                        "eur": 0.021,
                        "usd": 0.024
                      }
                    }
                    """, MediaType.APPLICATION_JSON));

            BigDecimal result = exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            );

            assertThat(result).isEqualByComparingTo("0.021");

            server.verify();
        }

        @Test
        void givenSameCurrency_whenGetRate_thenReturnOne() {
            CurrencyCode fromCurrency = CurrencyCode.EUR;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            BigDecimal result = exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            );

            assertThat(result).isEqualByComparingTo(BigDecimal.ONE);

            server.verify();
        }
    }

    @Nested
    class GetRateValidationTests {

        @Test
        void givenNullFromCurrency_whenGetRate_thenThrowIllegalArgumentException() {
            CurrencyCode fromCurrency = null;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency cannot be null");

            server.verify();
        }

        @Test
        void givenNullToCurrency_whenGetRate_thenThrowIllegalArgumentException() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = null;
            LocalDate date = LocalDate.of(2025, 7, 15);

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency cannot be null");

            server.verify();
        }

        @Test
        void givenNullDate_whenGetRate_thenThrowIllegalArgumentException() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = null;

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Date cannot be null");

            server.verify();
        }
    }

    @Nested
    class GetRateApiResponseTests {

        @Test
        void givenEmptyApiResponse_whenGetRate_thenThrowIllegalStateException() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            server.expect(requestTo(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2025-07-15/v1/currencies/usd.min.json"
            )).andRespond(withNoContent());

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Currency API response is empty");

            server.verify();
        }

        @Test
        void givenResponseWithoutBaseCurrencyNode_whenGetRate_thenThrowIllegalStateException() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            server.expect(requestTo(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2025-07-15/v1/currencies/usd.min.json"
            )).andRespond(withSuccess("""
                    {
                      "date": "2025-07-15",
                      "eur": {
                        "usd": 1.16
                      }
                    }
                    """, MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Rates not found for base currency: USD");

            server.verify();
        }

        @Test
        void givenResponseWithoutTargetCurrencyRate_whenGetRate_thenThrowIllegalStateException() {
            CurrencyCode fromCurrency = CurrencyCode.USD;
            CurrencyCode toCurrency = CurrencyCode.EUR;
            LocalDate date = LocalDate.of(2025, 7, 15);

            server.expect(requestTo(
                    "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@2025-07-15/v1/currencies/usd.min.json"
            )).andRespond(withSuccess("""
                    {
                      "date": "2025-07-15",
                      "usd": {
                        "uah": 41.50,
                        "gbp": 0.74
                      }
                    }
                    """, MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> exchangeRateService.getRate(
                    fromCurrency,
                    toCurrency,
                    date
            ))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Exchange rate not found: USD to EUR on 2025-07-15");

            server.verify();
        }
    }
}