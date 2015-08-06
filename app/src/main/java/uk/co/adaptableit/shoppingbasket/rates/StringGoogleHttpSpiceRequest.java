package uk.co.adaptableit.shoppingbasket.rates;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import roboguice.util.temp.Ln;

/**
 * Created by andrew.clark on 06/08/2015.
 */
public class StringGoogleHttpSpiceRequest extends GoogleHttpClientSpiceRequest<String> {

    private String url;

    public StringGoogleHttpSpiceRequest(String url) {
        super(String.class);

        this.url = url;
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
