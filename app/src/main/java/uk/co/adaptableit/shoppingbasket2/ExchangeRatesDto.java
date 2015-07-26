package uk.co.adaptableit.shoppingbasket2;

import java.util.Map;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ExchangeRatesDto {
    private Map<String, Double> rates;

    private String baseCurrency;

    private long timestamp;

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
