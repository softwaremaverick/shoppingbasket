package uk.co.adaptableit.shoppingbasket.rates;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ExchangeRatesRequest extends StringGoogleHttpSpiceRequest {

    public ExchangeRatesRequest(String baseUrl, String appId, String baseCurrency) {
        super(String.format("%s/latest.json?app_id=%s&base=%s", baseUrl, appId, baseCurrency));
    }
}
