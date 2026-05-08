package com.example.spendly.currency.common.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyCodeTest {

    @Nested
    class IsSameTests {

        @Test
        void givenExactCurrencyCode_whenIsSame_thenReturnTrue() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "EUR";

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isTrue();
        }

        @Test
        void givenLowerCaseCurrencyCode_whenIsSame_thenReturnTrue() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "eur";

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isTrue();
        }

        @Test
        void givenRandomCaseCurrencyCode_whenIsSame_thenReturnTrue() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "EuR";

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isTrue();
        }

        @Test
        void givenCurrencyCodeWithSpaces_whenIsSame_thenReturnTrue() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "     EUR        ";

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isTrue();
        }

        @Test
        void givenDifferentCurrencyCode_whenIsSame_thenReturnFalse() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "USD";

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @EnumSource(CurrencyCode.class)
        void givenSupportedCurrencyCode_whenIsSame_thenReturnTrue(CurrencyCode expectedCurrencyCode) {
            String value = expectedCurrencyCode.getAlphabeticCode();

            boolean result = expectedCurrencyCode.isSame(value);

            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "UER",
                "EU",
                "EURO",
                "123",
                "eur1",
                "ABC"
        })
        void givenUnsupportedCurrencyCode_whenIsSame_thenReturnFalse(String unsupportedCurrencyCode) {
            CurrencyCode currencyCode = CurrencyCode.EUR;

            boolean result = currencyCode.isSame(unsupportedCurrencyCode);

            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                " ",
                "   ",
                "\t",
                "\n",
                "\r"
        })
        void givenNullEmptyOrBlankValue_whenIsSame_thenReturnFalse(String value) {
            CurrencyCode currencyCode = CurrencyCode.EUR;

            boolean result = currencyCode.isSame(value);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class FromStringTests {

        @Test
        void givenExactCurrencyCode_whenFromString_thenReturnCurrencyCode() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "EUR";

            CurrencyCode result = CurrencyCode.fromString(value);

            assertThat(result).isEqualTo(expectedCurrencyCode);
        }

        @Test
        void givenLowerCaseCurrencyCode_whenFromString_thenReturnCurrencyCode() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "eur";

            CurrencyCode result = CurrencyCode.fromString(value);

            assertThat(result).isEqualTo(expectedCurrencyCode);
        }

        @Test
        void givenRandomCaseCurrencyCode_whenFromString_thenReturnCurrencyCode() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "EuR";

            CurrencyCode result = CurrencyCode.fromString(value);

            assertThat(result).isEqualTo(expectedCurrencyCode);
        }

        @Test
        void givenCurrencyCodeWithSpaces_whenFromString_thenReturnCurrencyCode() {
            CurrencyCode expectedCurrencyCode = CurrencyCode.EUR;
            String value = "     EUR     ";

            CurrencyCode result = CurrencyCode.fromString(value);

            assertThat(result).isEqualTo(expectedCurrencyCode);
        }

        @ParameterizedTest
        @EnumSource(CurrencyCode.class)
        void givenSupportedCurrencyCode_whenFromString_thenReturnExpectedCurrencyCode(
                CurrencyCode expectedCurrencyCode
        ) {
            String value = expectedCurrencyCode.getAlphabeticCode();

            CurrencyCode result = CurrencyCode.fromString(value);

            assertThat(result).isEqualTo(expectedCurrencyCode);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "UER",
                "EU",
                "EURO",
                "123",
                "eur1",
                "ABC"
        })
        void givenUnsupportedCurrencyCode_whenFromString_thenThrowIllegalArgumentException(
                String unsupportedCurrencyCode
        ) {
            assertThatThrownBy(() -> CurrencyCode.fromString(unsupportedCurrencyCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported currency code: " + unsupportedCurrencyCode);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                " ",
                "   ",
                "\t",
                "\n",
                "\r"
        })
        void givenNullEmptyOrBlankCurrencyCode_whenFromString_thenThrowIllegalArgumentException(
                String invalidCurrencyCode
        ) {
            assertThatThrownBy(() -> CurrencyCode.fromString(invalidCurrencyCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency code cannot be empty");
        }
    }
}