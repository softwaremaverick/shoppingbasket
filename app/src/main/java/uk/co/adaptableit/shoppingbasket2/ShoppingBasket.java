package uk.co.adaptableit.shoppingbasket2;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Andrew Clark on 25/07/2015.
 */
public class ShoppingBasket {

    private Set<CharSequence> selectedItems = new HashSet<>();
    private int selectedCost = 0;

    public ShoppingBasket() {

    }

    public ShoppingBasket(Set<CharSequence> itemNames, int itemCostInPence) {
        this.selectedItems = itemNames;
        this.selectedCost = itemCostInPence;
    }

    public void addItem (CharSequence name, int priceInPence) {
        if (!selectedItems.contains(name)) {
            selectedItems.add(name);
            selectedCost += priceInPence;
        }
    }

    public void removeItem (CharSequence name, int priceInPence) {
        if (selectedItems.contains(name)) {
            selectedItems.remove(name);
            selectedCost -= priceInPence;
        }
    }

    public Set<CharSequence> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedItemsCost() {
        return selectedCost;
    }

    public boolean contains (CharSequence name) {
        return selectedItems.contains(name);
    }
}
