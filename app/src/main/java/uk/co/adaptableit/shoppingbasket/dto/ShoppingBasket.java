package uk.co.adaptableit.shoppingbasket.dto;

import java.util.HashSet;
import java.util.Set;

import uk.co.adaptableit.shoppingbasket.ProductCatalogue;

/**
 * Created by Andrew Clark on 25/07/2015.
 */
public class ShoppingBasket {
    private Set<CharSequence> selectedItems = new HashSet<>();
    private int selectedCost = 0;
    private ProductCatalogue productCatalogue;

    public ShoppingBasket(ProductCatalogue productCatalogue) {
        this.productCatalogue = productCatalogue;
    }

    public ShoppingBasket(Set<CharSequence> itemNames, int itemCostInPence) {
        this.selectedItems = itemNames;
        this.selectedCost = itemCostInPence;
    }

    public void addItem(CharSequence itemCode) {
        if (!selectedItems.contains(itemCode)) {
            selectedCost += productCatalogue.getProduct(itemCode.toString()).getPriceInPence();

            selectedItems.add(itemCode);
        }
    }

    public void removeItem(CharSequence itemCode) {
        if (selectedItems.contains(itemCode)) {
            selectedCost -= productCatalogue.getProduct(itemCode.toString()).getPriceInPence();

            selectedItems.remove(itemCode);
        }
    }

    public Set<CharSequence> getSelectedItems() {
        return selectedItems;
    }

    public int getSelectedItemsCost() {
        return selectedCost;
    }

    public boolean containsCode(CharSequence name) {
        return selectedItems.contains(name);
    }
}
