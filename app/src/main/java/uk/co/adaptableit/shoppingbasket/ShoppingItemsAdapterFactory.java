package uk.co.adaptableit.shoppingbasket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.adaptableit.shoppingbasket2.R;

/**
 * Created by Andrew Clark on 25/07/2015.
 */
public class ShoppingItemsAdapterFactory {
    public static class RowData {
        private Item item;

        private CheckBox checkboxView;

        private RowData(CheckBox checkboxView) {
            this.checkboxView = checkboxView;
        }

        public CheckBox getCheckboxView() {
            return checkboxView;
        }

        public Item getItem() {
            return item;
        }
    }

    public static class Item extends HashMap<String, Object> {
        private static final String KEY_ITEM_CODE = "CODE";
        private static final String KEY_NAME = "NAME";
        private static final String KEY_PRICE = "PRICE";

        public Item(String itemCode, String name, int priceInPence) {
            this.put(KEY_ITEM_CODE, itemCode);
            this.put(KEY_NAME, name);
            this.put(KEY_PRICE, priceInPence);
        }

        public String getItemCode() {
            return (String) this.get(KEY_ITEM_CODE);
        }

        public int getPriceInPence() {
            return (int) this.get(KEY_PRICE);
        }

        public String getName() {
            return (String) this.get(KEY_NAME);
        }
    }

    public SimpleAdapter createItemAdapter(Context context, final int resource, int resItemNameId, int resItemPriceId) {
        List<Item> items = getProducts();
        final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return new SimpleAdapter(context, items, resource,
                new String[]{Item.KEY_NAME, Item.KEY_PRICE},
                new int[]{resItemNameId, resItemPriceId}) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View newConverterView;
                final RowData rowData;

                if (convertView == null) {
                    newConverterView = mInflater.inflate(resource, parent, false);

                    CheckBox checkboxView = (CheckBox) newConverterView.findViewById(R.id.cb_itemSelected);

                    rowData = new RowData(checkboxView);
                    newConverterView.setTag(rowData);

                } else {
                    newConverterView = convertView;

                    rowData = (RowData) convertView.getTag();
                }

                rowData.item = (Item) getItem(position);

                return super.getView(position, newConverterView, parent);
            }
        };
    }

    private List<Item> getProducts() {
        List<Item> items = new ArrayList<>();

        for (ProductCatalogue.Product product : ProductCatalogue.getProducts()) {
            items.add(new Item(product.getProductCode(), product.getName(), product.getPriceInPence()));
        }

        return items;
    }
}
