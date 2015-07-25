package uk.co.adaptableit.shoppingbasket2;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew Clark on 25/07/2015.
 */
public class ShoppingItemsAdapterFactory {
    private static String KEY_NAME = "NAME";
    private static String KEY_PRICE = "PRICE";

    public SimpleAdapter createItemAdapter(Context context, int resource, int resItemNameId, int resItemPriceId) {
        List<Map<String, Object>> items = new ArrayList<>();

        items.add(createItem("Peas", 95));
        items.add(createItem("Eggs", 210));
        items.add(createItem("Milk", 130));
        items.add(createItem("Beans", 73));

        SimpleAdapter simpleAdapter = new SimpleAdapter(context, items, resource, new String[] {KEY_NAME, KEY_PRICE}, new int[] {resItemNameId, resItemPriceId});
        return simpleAdapter;
    }

    private Map<String, Object> createItem(String name, int priceInPence) {
        Map<String, Object> item = new HashMap<>();
        item.put(KEY_NAME, name);
        item.put(KEY_PRICE, new Integer(priceInPence));

        return item;
    }
}
