package bisq.price;

import bisq.price.spot.ExchangeRate;
import bisq.price.spot.ExchangeRateProvider;

import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.TradeCurrency;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertTrue;

@Slf4j
public class ExchangeTestBase {

    protected void doGet_successfulCall(ExchangeRateProvider exchangeProvider) {

        // Use the XChange library to call the provider API, in order to retrieve the
        // exchange rates. If the API call fails, or the response body cannot be parsed,
        // the test will fail with an exception
        Set<ExchangeRate> retrievedExchangeRates = exchangeProvider.doGet();

        // Log the valid exchange rates which were retrieved
        // Useful when running the tests, to easily identify which exchanges provide useful pairs
        retrievedExchangeRates.forEach(e -> log.info("Found exchange rate " + e.toString()));

        // Sanity checks
        assertTrue(retrievedExchangeRates.size() > 0);
        checkProviderCurrencyPairs(retrievedExchangeRates);
    }

    /**
     * Check that every retrieved currency pair is between BTC and either
     * A) a fiat currency on the list of Bisq-supported fiat currencies, or
     * B) an altcoin on the list of Bisq-supported altcoins
     *
     * @param retrievedExchangeRates Exchange rates retrieved from the provider
     */
    private void checkProviderCurrencyPairs(Set<ExchangeRate> retrievedExchangeRates) {
        Set<String> retrievedRatesCurrencies = retrievedExchangeRates.stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        Set<String> supportedCryptoCurrencies = CurrencyUtil.getAllSortedCryptoCurrencies().stream()
                .map(TradeCurrency::getCode)
                .collect(Collectors.toSet());

        Set<String> supportedFiatCurrencies = CurrencyUtil.getAllSortedFiatCurrencies().stream()
                .map(TradeCurrency::getCode)
                .collect(Collectors.toSet());

        Set<String> supportedFiatCurrenciesRetrieved = supportedFiatCurrencies.stream()
                .filter(f -> retrievedRatesCurrencies.contains(f))
                .collect(Collectors.toCollection(TreeSet::new));
        log.info("Retrieved rates for supported fiat currencies: " + supportedFiatCurrenciesRetrieved);

        Set<String> supportedCryptoCurrenciesRetrieved = supportedCryptoCurrencies.stream()
                .filter(c -> retrievedRatesCurrencies.contains(c))
                .collect(Collectors.toCollection(TreeSet::new));
        log.info("Retrieved rates for supported altcoins: " + supportedCryptoCurrenciesRetrieved);

        Set<String> supportedCurrencies = Sets.union(supportedCryptoCurrencies, supportedFiatCurrencies);

        Set unsupportedCurrencies = Sets.difference(retrievedRatesCurrencies, supportedCurrencies);
        assertTrue("Retrieved exchange rates contain unsupported currencies: " + unsupportedCurrencies,
                unsupportedCurrencies.isEmpty());
    }
}
