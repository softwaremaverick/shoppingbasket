package uk.co.adaptableit.shoppingbasket;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Created by Andrew Clark on 26/07/2015.
 */
public class ProductCatalogue {

    public static class Product {
        private String productCode;
        private String name;
        private int priceInPence;

        public Product(String productCode, String name, int priceInPence) {
            this.productCode = productCode;
            this.name = name;
            this.priceInPence = priceInPence;
        }

        public String getProductCode() {
            return productCode;
        }

        public String getName() {
            return name;
        }

        public int getPriceInPence() {
            return priceInPence;
        }
    }

    private static String currencyCode;

    private static final LinkedHashMap<String, Product> productMap = new LinkedHashMap<>();

    static {
        currencyCode = "USD";

        productMap.put("1", new Product("1", "Peas", 95));
        productMap.put("2", new Product("2", "Eggs", 210));
        productMap.put("3", new Product("3", "Milk", 130));
        productMap.put("4", new Product("4", "Beans", 73));
    }

    private static ProductCatalogue INSTANCE = new ProductCatalogue();

    public static ProductCatalogue getInstance() {
        return INSTANCE;
    }

    public Collection<Product> getProducts() {
        return productMap.values();
    }

    public Product getProduct(String productCode) {
        return productMap.get(productCode);
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
