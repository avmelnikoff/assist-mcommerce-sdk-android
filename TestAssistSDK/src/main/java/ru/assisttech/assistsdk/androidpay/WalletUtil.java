package ru.assisttech.assistsdk.androidpay;


import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to create {@link MaskedWalletRequest}, {@link FullWalletRequest} as well as
 * {@link NotifyTransactionStatusRequest} objects
 */
public class WalletUtil {

    private WalletUtil() {}

    /**
     * Creates a MaskedWalletRequest for direct merchant integration (no payment processor)
     *
     * @param itemInfo {@link ItemInfo} containing details of an item.
     * @param publicKey base64-encoded public encryption key. See instructions for more details.
     * @return {@link MaskedWalletRequest} instance
     */
    public static MaskedWalletRequest createMaskedWalletRequest(ItemInfo itemInfo, String publicKey) {
        // Validate the public key
        if (publicKey == null || publicKey.contains("REPLACE_ME")) {
            throw new IllegalArgumentException("Invalid public key, see README for instructions.");
        }

        // Create direct integration parameters
        // [START direct_integration_parameters]
        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                        .addParameter("publicKey", publicKey)
                        .build();
        // [END direct_integration_parameters]

        return createMaskedWalletRequest(itemInfo, parameters);
    }

    private static MaskedWalletRequest createMaskedWalletRequest(ItemInfo itemInfo, PaymentMethodTokenizationParameters parameters) {
        // Build a List of all line items
        List<LineItem> lineItems = buildLineItems(itemInfo);

        // Calculate the cart total by iterating over the line items.
        String cartTotal = calculateCartTotal(lineItems);

        // [START masked_wallet_request]
        MaskedWalletRequest request = MaskedWalletRequest.newBuilder()
                .setMerchantName(itemInfo.merchantName)
                .setPhoneNumberRequired(false)
                .setShippingAddressRequired(false)
                .setCurrencyCode(itemInfo.currencyCode)
                .setEstimatedTotalPrice(cartTotal)
                // Create a Cart with the current line items. Provide all the information
                // available up to this point with estimates for shipping and tax included.
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(itemInfo.currencyCode)
                        .setTotalPrice(cartTotal)
                        .setLineItems(lineItems)
                        .build())
                .setPaymentMethodTokenizationParameters(parameters)
                .build();

        return request;
        // [END masked_wallet_request]
    }

    /**
     * Build a list of line items based on the {@link ItemInfo} and a boolean that indicates
     * whether to use estimated values of tax and shipping for setting up the
     * {@link MaskedWalletRequest} or actual values in the case of a {@link FullWalletRequest}
     *
     * @param itemInfo {@link ItemInfo} used for building the
     *                 {@link com.google.android.gms.wallet.LineItem} list.
     * @return list of line items
     */
    private static List<LineItem> buildLineItems(ItemInfo itemInfo) {
        List<LineItem> list = new ArrayList<LineItem>();

        list.add(LineItem.newBuilder()
                .setCurrencyCode(itemInfo.currencyCode)
                .setDescription(itemInfo.name)
                .setQuantity(itemInfo.quantity)
                .setUnitPrice(formatPrice(itemInfo.price))
                .setTotalPrice(formatPrice(itemInfo.getTotalPrice()))
                .build());

        list.add(LineItem.newBuilder()
                .setCurrencyCode(itemInfo.currencyCode)
                .setDescription(itemInfo.shippingDescription)
                .setRole(LineItem.Role.SHIPPING)
                .setTotalPrice(formatPrice(itemInfo.shippingPrice))
                .build());

        list.add(LineItem.newBuilder()
                .setCurrencyCode(itemInfo.currencyCode)
                .setDescription(itemInfo.taxDescription)
                .setRole(LineItem.Role.TAX)
                .setTotalPrice(formatPrice(itemInfo.tax))
                .build());

        return list;
    }

    /**
     * @param lineItems List of {@link com.google.android.gms.wallet.LineItem} used for calculating
     *                  the cart total.
     * @return cart total.
     */
    private static String calculateCartTotal(List<LineItem> lineItems) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        // Calculate the total price by adding up each of the line items
        for (LineItem lineItem: lineItems) {
            BigDecimal lineItemTotal =
                    lineItem.getTotalPrice() == null
                    ? new BigDecimal(lineItem.getUnitPrice()).multiply(new BigDecimal(lineItem.getQuantity()))
                    : new BigDecimal(lineItem.getTotalPrice());
            cartTotal = cartTotal.add(lineItemTotal);
        }
        return cartTotal.setScale(2, RoundingMode.HALF_EVEN).toString();
    }

    /**
     *
     * @param itemInfo {@link ItemInfo} to use for creating
     *                 the {@link com.google.android.gms.wallet.FullWalletRequest}
     * @param googleTransactionId
     * @return {@link FullWalletRequest} instance
     */
    public static FullWalletRequest createFullWalletRequest(ItemInfo itemInfo, String googleTransactionId) {

        List<LineItem> lineItems = buildLineItems(itemInfo);

        String cartTotal = calculateCartTotal(lineItems);

        // [START full_wallet_request]
        FullWalletRequest request = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(itemInfo.currencyCode)
                        .setTotalPrice(cartTotal)
                        .setLineItems(lineItems)
                        .build())
                .build();
        // [END full_wallet_request]

        return request;
    }

    /**
     * @param googleTransactionId
     * @param status from {@link NotifyTransactionStatusRequest.Status} which could either be
     *               {@code NotifyTransactionStatusRequest.Status.SUCCESS} or one of the error codes
     *               from {@link NotifyTransactionStatusRequest.Status.Error}
     * @return {@link NotifyTransactionStatusRequest} instance
     */
    @SuppressWarnings("javadoc")
    public static NotifyTransactionStatusRequest createNotifyTransactionStatusRequest(
            String googleTransactionId, int status) {
        return NotifyTransactionStatusRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setStatus(status)
                .build();
    }

    /**
     * @return string formatted as "0.00" required by the Instant Buy API.
     */
    private static String formatPrice(String price) {
        return new BigDecimal(price).setScale(2, RoundingMode.HALF_EVEN).toString();
    }
}
