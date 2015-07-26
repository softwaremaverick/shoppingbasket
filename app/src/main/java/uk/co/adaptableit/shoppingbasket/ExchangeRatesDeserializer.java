package uk.co.adaptableit.shoppingbasket;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ExchangeRatesDeserializer {

    public static ExchangeRatesDto deserialize(String json) throws JSONException {
        JSONObject rootNode = (JSONObject) new JSONTokener(json).nextValue();
        JSONObject ratesNode = (JSONObject) rootNode.get("rates");

        Iterator<String> keyIterator = ratesNode.keys();
        String value;

        Map<String, Double> rates = new HashMap<>();

        while (keyIterator.hasNext()) {
            value = keyIterator.next();
            rates.put(value, ratesNode.getDouble(value));
        }

        ExchangeRatesDto result = new ExchangeRatesDto();
        result.setBaseCurrency(rootNode.getString("base"));
        result.setTimestamp(rootNode.getLong("timestamp"));
        result.setRates(rates);
        return result;
    }
}
