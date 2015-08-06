package uk.co.adaptableit.shoppingbasket.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew.clark on 06/08/2015.
 */
public class CurrenciesDto {
    private Map<String, String> currencyCodeDescriptions = new HashMap<>();


    public Map<String, String> getCurrencyCodeDescriptions() {
        return currencyCodeDescriptions;
    }

    public void setCurrencyCodeDescriptions(Map<String, String> currencyCodeDescriptions) {
        this.currencyCodeDescriptions = currencyCodeDescriptions;
    }
}
