package uk.co.adaptableit.shoppingbasket;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import uk.co.adaptableit.shoppingbasket.dto.CurrenciesDto;
import uk.co.adaptableit.shoppingbasket.dto.ExchangeRatesDto;
import uk.co.adaptableit.shoppingbasket.dto.ShoppingBasket;
import uk.co.adaptableit.shoppingbasket.rates.CurrenciesDeserializer;
import uk.co.adaptableit.shoppingbasket.rates.CurrenciesRequest;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesDeserializer;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesRequest;
import uk.co.adaptableit.shoppingbasket2.R;


public class ItemSelectionActivity extends AppCompatActivity {

    private static final String TAG = ItemSelectionActivity.class.getName();

    private static final String SAVED_BASKET = "SAVED_BASKET";
    private static final String SELECTED_FX_CURRENCY_CODE = "GBP";

    private static final String CURRENCIES_CACHE_KEY = "CURRENCIES";

    private CurrencyFormatter currencyFormatter = CurrencyFormatter.getInstance();

    private ProductCatalogue productCatalogue = ProductCatalogue.getInstance();

    private CurrenciesDto currencyCodeDescriptions;

    private RequestListener<String> CURRENCY_CODES_LISTENER = new RequestListener<String>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(ItemSelectionActivity.this, R.string.text_CURRENCY_CODES_FAILED, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(String jsonResponse) {
            try {
                ItemSelectionActivity.this.currencyCodeDescriptions = CurrenciesDeserializer.deserialize(jsonResponse);

            } catch (JSONException e) {
                Log.e(TAG, "Error downloading currency codes", e);

                Toast.makeText(ItemSelectionActivity.this, R.string.text_CURRENCY_CODES_FAILED, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private RequestListener<String> EXCHANGE_RATES_LISTENER = new RequestListener<String>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(ItemSelectionActivity.this, R.string.text_FX_FAILED, Toast.LENGTH_SHORT).show();
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

    private ShoppingBasket basket = new ShoppingBasket(productCatalogue);

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
                productCatalogue,
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

                        ((TextView) view).setText(currencyFormatter.formatPennies((Integer) data, productCatalogue.getCurrencyCode()));

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

    private void requestExchangeRates() {
        String baseUrl = getString(R.string.rates_api_baseurl);
        String appId = getString(R.string.rates_api_app_id);
        String baseCurrenccy = productCatalogue.getCurrencyCode();

        manager.execute(new ExchangeRatesRequest(baseUrl, appId, baseCurrenccy), baseCurrenccy, DurationInMillis.ONE_HOUR, EXCHANGE_RATES_LISTENER);
        manager.execute(new CurrenciesRequest(baseUrl, appId), CURRENCIES_CACHE_KEY, DurationInMillis.ONE_WEEK, CURRENCY_CODES_LISTENER);
    }

    private void updatePrice() {
        costTextView.setText(currencyFormatter.formatPennies(basket.getSelectedItemsCost(), productCatalogue.getCurrencyCode()));

        if (exchangeRates == null) {
            fxCostTextView.setText(R.string.text_NOFX);
            fxCostTextView.setTextColor(Color.RED);
        } else {
            double fxPrice = currencyFormatter.calculateFXPrice(basket.getSelectedItemsCost(), SELECTED_FX_CURRENCY_CODE, exchangeRates);

            fxCostTextView.setText(currencyFormatter.getNumberFormat(SELECTED_FX_CURRENCY_CODE).format(fxPrice) + " " + SELECTED_FX_CURRENCY_CODE);
            fxCostTextView.setTextColor(Color.BLUE);
        }
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
        getMenuInflater().inflate(R.menu.menu_item_selection, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);

        requestExchangeRates();
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
                final String baseCost = currencyFormatter.formatPennies(basket.getSelectedItemsCost(), productCatalogue.getCurrencyCode());
                String totalCostString = null;

                if (exchangeRates != null) {
                    Double fxPrice = currencyFormatter.calculateFXPrice(basket.getSelectedItemsCost(), SELECTED_FX_CURRENCY_CODE, exchangeRates);

                    if (fxPrice != null) {
                        String fxCostString = currencyFormatter.getNumberFormat(SELECTED_FX_CURRENCY_CODE).format(fxPrice) + " " + SELECTED_FX_CURRENCY_CODE;

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
