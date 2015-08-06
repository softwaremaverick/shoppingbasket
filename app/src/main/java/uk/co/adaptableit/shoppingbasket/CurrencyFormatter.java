package uk.co.adaptableit.shoppingbasket;

import android.support.v7.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

import uk.co.adaptableit.shoppingbasket.dto.ExchangeRatesDto;

/**
 * Created by andrew.clark on 06/08/2015.
 */
public class CurrencyFormatter extends AppCompatActivity {

    private final static CurrencyFormatter INSTANCE = new CurrencyFormatter();

    private CurrencyFormatter() {
    }

    public static CurrencyFormatter getInstance() {
        return INSTANCE;
    }

    public String formatPennies(Integer priceInPence, String currencyCode) {
        float costInPounds = (float) (priceInPence / 100.0);

        return getNumberFormat(currencyCode).format(costInPounds);
    }

    public Double calculateFXPrice(int basePriceInPennies, String currencyCode, ExchangeRatesDto exchangeRates) {
        Double fxRate = exchangeRates.getRates().get(currencyCode);
        Double result = null;

        if (fxRate != null) {
            result = Double.valueOf((double) (basePriceInPennies * fxRate) / 100);
        }

        return result;
    }

    public NumberFormat getNumberFormat(String currencyCode) {
        switch (currencyCode) {
            case "GBP":
                return NumberFormat.getCurrencyInstance(Locale.UK);

            case "USD":
                return NumberFormat.getCurrencyInstance(Locale.US);

            default:
                return null;
        }
    }
}
