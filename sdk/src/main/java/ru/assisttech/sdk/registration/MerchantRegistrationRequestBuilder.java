package ru.assisttech.sdk.registration;


/**
 * Builds request for merchant registration
 */
public class MerchantRegistrationRequestBuilder extends RegistrationRequestBuilder {

    private static final String MERCHANT_ID = "Merchant_ID";
    private static final String SHOP = "Shop";

    public MerchantRegistrationRequestBuilder(AssistRegistrationData data) {
        super(data);
    }

    @Override
    void modifyRequest(AssistRegistrationData data, StringBuilder request) {

        /* Add new fields */
        request.append("<" + SHOP + ">");
        request.append("1");
        request.append("</" + SHOP + ">");

        request.append("<" + MERCHANT_ID + ">");
        request.append(data.getMerchantID());
        request.append("</" + MERCHANT_ID + ">");
    }
}
