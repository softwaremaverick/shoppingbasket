package uk.co.adaptableit.shoppingbasket;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

import uk.co.adaptableit.shoppingbasket.dto.ExchangeRatesDto;
import uk.co.adaptableit.shoppingbasket.dto.ShoppingBasket;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesDeserializer;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesRequest;
import uk.co.adaptableit.shoppingbasket2.R;


public class ItemSelectionActivity extends ActionBarActivity {

    private static final String TAG = ItemSelectionActivity.class.getName();

    private static final String SAVED_BASKET = "SAVED_BASKET";

    private static final NumberFormat UK_CURRENCY_INSTANCE = NumberFormat.getCurrencyInstance(Locale.UK);
    private static final NumberFormat US_CURRENCY_INSTANCE = NumberFormat.getCurrencyInstance(Locale.US);

    private RequestListener<String> EXCHANGE_RATES_LISTENER = new RequestListener<String>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(ItemSelectionActivity.this, "fAILURE", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(String jsonResponse) {
            try {
                ItemSelectionActivity.this.exchangeRates = ExchangeRatesDeserializer.deserialize(jsonResponse);

                if (basket != null) {
                    updatePrice();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error downloading exchange rates", e);

                Toast.makeText(ItemSelectionActivity.this, R.string.text_FX_FAILED, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private SpiceManager manager = new SpiceManager(NetworkRequestService.class);

    private ShoppingBasket basket = new ShoppingBasket();

    private ExchangeRatesDto exchangeRates;

    private SimpleAdapter adapter;
    private TextView costTextView;
    private TextView fxCostTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Object previousData = this.getLastCustomNonConfigurationInstance();

        if (previousData != null) {
            basket = (ShoppingBasket) previousData;
        } else {
            if (savedInstanceState != null) {
                basket = ShoppingBasketBundleMapper.createBasket(savedInstanceState.getBundle(SAVED_BASKET));
            }
        }

        setContentView(R.layout.activity_item_selection);

        costTextView = (TextView) findViewById(R.id.text_totalCost);
        fxCostTextView = (TextView) findViewById(R.id.text_fxCost);

        updatePrice();

        ListView items = (ListView) findViewById(R.id.list_items);

        adapter = new ShoppingItemsAdapterFactory().createItemAdapter(this,
                R.layout.view_tem,
                R.id.text_itemname,
                R.id.text_itemprice);

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                switch (view.getId()) {
                    case R.id.text_itemprice:
                        View viewParent = (View) view.getParent();
                        ShoppingItemsAdapterFactory.RowData rowData = (ShoppingItemsAdapterFactory.RowData) viewParent.getTag();

                        ((TextView) view).setText(convertPenceToCurrency((Integer) data, US_CURRENCY_INSTANCE));

                        boolean selected = basket.containsCode(rowData.getItem().getItemCode());
                        rowData.getCheckboxView().setChecked(selected);

                        return true;
                }

                return false;
            }
        });

        items.setAdapter(adapter);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShoppingItemsAdapterFactory.RowData rowData = (ShoppingItemsAdapterFactory.RowData) view.getTag();
                String itemCode = rowData.getItem().getItemCode();

                if (basket.containsCode(itemCode)) {
                    Log.d(TAG, "Item " + itemCode + " being removed from basket");

                    basket.removeItem(itemCode);
                    rowData.getCheckboxView().setChecked(false);
                } else {
                    Log.d(TAG, "Item " + itemCode + " being added to the basket");

                    basket.addItem(itemCode);
                    rowData.getCheckboxView().setChecked(true);
                }

                updatePrice();
            }
        });
    }

    private String convertPenceToCurrency(Integer priceInPence, NumberFormat numberFormatter) {
        float costInPounds = (float) (priceInPence / 100.0);

        return numberFormatter.format(costInPounds);
    }

    private void updatePrice() {
        costTextView.setText(convertPenceToCurrency(basket.getSelectedItemsCost(), US_CURRENCY_INSTANCE));

        if (exchangeRates == null) {
            fxCostTextView.setText(R.string.text_NOFX);
            fxCostTextView.setTextColor(Color.RED);
        } else {
            double fxPrice = calculateFXPrice("GBP", basket.getSelectedItemsCost());

            fxCostTextView.setText(UK_CURRENCY_INSTANCE.format(fxPrice));
            fxCostTextView.setTextColor(Color.BLUE);
        }
    }

    private Double calculateFXPrice(String currencyCode, int basePriceInPennies) {
        Double fxRate = exchangeRates.getRates().get(currencyCode);
        Double result = null;

        if (fxRate != null) {
            result = Double.valueOf((double) (basePriceInPennies * fxRate) / 100);
        }

        return result;
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return basket;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBundle(SAVED_BASKET, ShoppingBasketBundleMapper.createBundle(basket));

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_selection, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);

        requestExchangeRates();
    }

    private void requestExchangeRates() {
        String baseUrl = getString(R.string.rates_api_baseurl);
        String appId = getString(R.string.rates_api_app_id);
        String baseCurrenccy = getString(R.string.rates_api_base_currency);

        manager.execute(new ExchangeRatesRequest(baseUrl, appId, baseCurrenccy), baseCurrenccy, DurationInMillis.ONE_HOUR, EXCHANGE_RATES_LISTENER);
    }

    @Override
    protected void onStop() {
        super.onStop();

        manager.shouldStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_checkout:
                final String baseCost = convertPenceToCurrency(basket.getSelectedItemsCost(), US_CURRENCY_INSTANCE);
                String totalCostString = null;

                if (exchangeRates != null) {
                    Double fxPrice = calculateFXPrice("GBP", basket.getSelectedItemsCost());

                    if (fxPrice != null) {
                        String fxCostString = UK_CURRENCY_INSTANCE.format(fxPrice);

                        totalCostString = String.format("%s (%s)", baseCost, fxCostString);
                    }
                }

                if (totalCostString == null) {
                    totalCostString = String.format("%s (-)", baseCost);
                }

                String totalTextString = getString(R.string.text_total);
                Toast.makeText(this, totalTextString + ": " + totalCostString, Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
