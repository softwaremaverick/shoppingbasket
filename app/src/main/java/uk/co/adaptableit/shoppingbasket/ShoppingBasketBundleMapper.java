package uk.co.adaptableit.shoppingbasket;

import android.os.Bundle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.adaptableit.shoppingbasket.dto.ShoppingBasket;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ShoppingBasketBundleMapper {
    private static final String SAVED_SELECTED_ITEMS = "SELECTED_ITEMS";
    private static final String SAVED_SELECTED_ITEMS_COST = "SELECTED_ITEMS_COST";

    public static Bundle createBundle(ShoppingBasket basket) {
        Bundle bundle = new Bundle();

        Set<CharSequence> selectedItems = basket.getSelectedItems();
        bundle.putStringArray(SAVED_SELECTED_ITEMS, selectedItems.toArray(new String[selectedItems.size()]));
        bundle.putInt(SAVED_SELECTED_ITEMS_COST, basket.getSelectedItemsCost());

        return bundle;
    }

    public static ShoppingBasket createBasket(Bundle bundle) {
        ShoppingBasket basket = new ShoppingBasket(ProductCatalogue.getInstance());
        String[] selectedItems = bundle.getStringArray(SAVED_SELECTED_ITEMS);

        if (selectedItems != null) {
            int selectedItemCost = bundle.getInt(SAVED_SELECTED_ITEMS_COST);
            Set<CharSequence> selectedItemsSet = new HashSet<>();
            selectedItemsSet.addAll(Arrays.asList(selectedItems));

            basket = new ShoppingBasket(selectedItemsSet, selectedItemCost);
        }

        return basket;
    }
}
