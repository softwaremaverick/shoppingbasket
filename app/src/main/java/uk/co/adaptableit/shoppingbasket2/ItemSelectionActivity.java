package uk.co.adaptableit.shoppingbasket2;

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
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ItemSelectionActivity extends ActionBarActivity {

    public static final String SAVED_SELECTED_ITEMS = "SELECTED_ITEMS";
    public static final String SAVED_SELECTED_ITEMS_COST = "SELECTED_ITEMS_COST";

    private SpiceManager manager = new SpiceManager(NetworkRequestService.class);

    private ShoppingBasket basket = new ShoppingBasket();

    private SimpleAdapter adapter;
    private TextView costTextView;

    class NetworkListener implements RequestListener<Boolean> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            TextView tv = (TextView) ItemSelectionActivity.this.findViewById(R.id.shopping);
            tv.setText("Failure");
        }

        @Override
        public void onRequestSuccess(Boolean aBoolean) {
            TextView tv = (TextView) ItemSelectionActivity.this.findViewById(R.id.shopping);
            tv.setText("Success");
        }
    }

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

        SpiceRequest request = new ExchangeRatesRequest();

        manager.execute(request, new RequestListener<ExchangeRatesDto>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Toast.makeText(ItemSelectionActivity.this, "fAILURE", Toast.LENGTH_SHORT).show();;
            }

            @Override
            public void onRequestSuccess(ExchangeRatesDto o) {
                Toast.makeText(ItemSelectionActivity.this, "Success " + o.toString(), Toast.LENGTH_SHORT).show();

                Toast.makeText(ItemSelectionActivity.this, "UKP=" + o.getRates().get("GBP").toString(), Toast.LENGTH_SHORT).show();;
            }
        });
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
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void submit(View v) {
        manager.execute(new TestRequest(), new NetworkListener());
    }

}
