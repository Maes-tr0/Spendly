package com.example.spendly.currency.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyApiConfigTest {

    @Test
    void givenCurrencyApiConfig_whenCurrencyApiRestClient_thenReturnRestClientBean() {
        CurrencyApiConfig currencyApiConfig = new CurrencyApiConfig();

        RestClient result = currencyApiConfig.currencyApiRestClient();

        assertThat(result).isNotNull();
    }
}