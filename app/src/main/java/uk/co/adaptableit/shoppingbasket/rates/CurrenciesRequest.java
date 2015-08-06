package uk.co.adaptableit.shoppingbasket.rates;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class CurrenciesRequest extends StringGoogleHttpSpiceRequest {

    public CurrenciesRequest(String baseUrl, String appId) {
        super(String.format("%s/currencies.json?app_id=%s", baseUrl, appId));
    }
}
