package uk.co.adaptableit.shoppingbasket;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.adaptableit.shoppingbasket.dto.ExchangeRatesDto;
import uk.co.adaptableit.shoppingbasket.dto.ShoppingBasket;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesDeserializer;
import uk.co.adaptableit.shoppingbasket.rates.ExchangeRatesRequest;
import uk.co.adaptableit.shoppingbasket2.R;


public class ItemSelectionActivity extends ActionBarActivity {

    public static final String SAVED_SELECTED_ITEMS = "SELECTED_ITEMS";
    public static final String SAVED_SELECTED_ITEMS_COST = "SELECTED_ITEMS_COST";

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
                CharSequence[] savedSelectedItems = savedInstanceState.getStringArray(SAVED_SELECTED_ITEMS);
                int savedSelectedItemsCost = savedInstanceState.getInt(SAVED_SELECTED_ITEMS_COST);

                if (savedSelectedItems != null) {
                    basket = new ShoppingBasket(new HashSet<>(Arrays.asList(savedSelectedItems)), savedSelectedItemsCost);
                }
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
                if (view.getId() == R.id.text_itemname) {
                    if (basket.contains((CharSequence) data)) {
                        view.setBackgroundColor(Color.BLUE);
                    } else {
                        view.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
                return false;
            }
        });

        items.setAdapter(adapter);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView nameView = (TextView) view.findViewById(R.id.text_itemname);
                TextView priceView = (TextView) view.findViewById(R.id.text_itemprice);
                CharSequence itemName = nameView.getText();

                if (basket.contains(itemName)) {
                    basket.removeItem(itemName, Integer.parseInt(priceView.getText().toString()));
                    nameView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    basket.addItem(itemName, Integer.parseInt(priceView.getText().toString()));
                    nameView.setBackgroundColor(Color.BLUE);
                }

                updatePrice();
            }
        });
    }

    private void updatePrice() {
        costTextView.setText(Integer.toString(basket.getSelectedItemsCost()));

        if (exchangeRates == null) {
            fxCostTextView.setText(R.string.text_NOFX);
            fxCostTextView.setTextColor(Color.RED);
        } else {
            Double ukExchangeRate = exchangeRates.getRates().get("GBP");
            double fxPrice = basket.getSelectedItemsCost() * ukExchangeRate;

            NumberFormat numberFormat = DecimalFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);

            fxCostTextView.setText(numberFormat.format(fxPrice));
            fxCostTextView.setTextColor(Color.BLUE);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return basket;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Set<CharSequence> selectedItems = basket.getSelectedItems();

        outState.putStringArray(SAVED_SELECTED_ITEMS, selectedItems.toArray(new String[selectedItems.size()]));
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
        String appId = "3553795c5e314300b22c71b95f647225";
        String baseCurrenccy = "USD";

        manager.execute(new ExchangeRatesRequest(appId, baseCurrenccy), baseCurrenccy, DurationInMillis.ONE_HOUR, EXCHANGE_RATES_LISTENER);
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
                Toast.makeText(this, "Total Cost: " + Integer.toString(basket.getSelectedItemsCost()), Toast.LENGTH_SHORT).show();
                requestExchangeRates();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
