package uk.co.adaptableit.shoppingbasket.rates;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uk.co.adaptableit.shoppingbasket.dto.CurrenciesDto;
import uk.co.adaptableit.shoppingbasket.dto.ExchangeRatesDto;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class CurrenciesDeserializer {

    public static CurrenciesDto deserialize(String json) throws JSONException {
        JSONObject rootNode = (JSONObject) new JSONTokener(json).nextValue();

        Iterator<String> keyIterator = rootNode.keys();
        String value;

        Map<String, String> currencyCodeDescriptions = new HashMap<>();

        while (keyIterator.hasNext()) {
            value = keyIterator.next();

            currencyCodeDescriptions.put(value, rootNode.getString(value));
        }

        CurrenciesDto result = new CurrenciesDto();
        result.setCurrencyCodeDescriptions(currencyCodeDescriptions);

        return result;
    }
}
