package ru.assisttech.sdk.storage;

import java.util.Locale;

public class AssistOrderItem {

    private String name;
    private String quantity;
    private String unitPrice;

    public AssistOrderItem(String name, String quantity, String unitPrice) {
        this.name = name;
        this.quantity = quantity.replace(",", ".");
        this.unitPrice = processPrice(unitPrice.replace(",", "."));
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getItemPrice() {
        Float up = Float.valueOf(unitPrice);
        Float q = Float.valueOf(quantity);
        return String.format(Locale.US, "%1$3.2f", up * q);
    }

    private String processPrice(String price) {
        Float p = Float.valueOf(price);
        return String.format(Locale.US, "%1$3.2f", p);
    }
}
