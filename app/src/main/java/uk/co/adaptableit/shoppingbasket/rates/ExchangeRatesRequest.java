package uk.co.adaptableit.shoppingbasket.rates;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import roboguice.util.temp.Ln;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ExchangeRatesRequest extends GoogleHttpClientSpiceRequest<String> {

    private String url;
    private static String TAG = ExchangeRatesRequest.class.getName();

    public ExchangeRatesRequest(String baseUrl, String appId, String baseCurrency) {
        super(java.lang.String.class);

        this.url = String.format("%s/latest.json?app_id=%s&base=%s", baseUrl, appId, baseCurrency);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        Ln.d("Call web service " + url);
        HttpRequest request = getHttpRequestFactory()//
                .buildGetRequest(new GenericUrl(url));

        HttpResponse response = request.execute();
        String result = null;

        if (response.getStatusCode() != 200) {
            throw new Exception("HTTP Failed");
        } else {
            result = response.parseAsString();
        }

        return result;
    }
}
