package uk.co.adaptableit.shoppingbasket2;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import roboguice.util.temp.Ln;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ExchangeRatesRequest extends GoogleHttpClientSpiceRequest<ExchangeRatesDto> {

    private String baseUrl;
    private static String TAG = ExchangeRatesRequest.class.getName();

    ExchangeRatesRequest() {
        super(ExchangeRatesDto.class);

        baseUrl = "https://openexchangerates.org/api/latest.json?app_id=3553795c5e314300b22c71b95f647225&base=USD";
    }

    @Override
    public ExchangeRatesDto loadDataFromNetwork() throws Exception {
        Ln.d("Call web service " + baseUrl);
        HttpRequest request = getHttpRequestFactory()//
                .buildGetRequest(new GenericUrl(baseUrl));

        HttpResponse response = request.execute();

        ExchangeRatesDto result = null;

        if (response.getStatusCode() != 200) {
            throw new Exception("HTTP Failed");
        } else {
            String json = response.parseAsString();

            JSONObject rootNode = (JSONObject) new JSONTokener(json).nextValue();
            JSONObject ratesNode = (JSONObject) rootNode.get("rates");

            Iterator<String> keyIterator = ratesNode.keys();
            String value;

            Map<String,Double> rates = new HashMap<>();

            while (keyIterator.hasNext()) {
                value = keyIterator.next();
                rates.put(value, ratesNode.getDouble(value));
            }

            result = new ExchangeRatesDto();
            result.setBaseCurrency(rootNode.getString("base"));
            result.setTimestamp(rootNode.getLong("timestamp"));
            result.setRates(rates);
        }

        return result;
    }
}
