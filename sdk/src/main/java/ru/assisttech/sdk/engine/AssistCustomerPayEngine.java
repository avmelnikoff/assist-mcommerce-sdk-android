package ru.assisttech.sdk.engine;

import android.app.Activity;
import android.content.Context;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.registration.AssistRegistrationData;
import ru.assisttech.sdk.registration.CustomerRegistrationRequestBuilder;
import ru.assisttech.sdk.registration.RegistrationRequestBuilder;
import ru.assisttech.sdk.processor.AssistResultProcessor;
import ru.assisttech.sdk.processor.AssistProcessorEnvironment;

public class AssistCustomerPayEngine extends AssistBasePayEngine {

    private static AssistCustomerPayEngine instance;
    private static final String DB_NAME_SUFFIX = "c";

    public static synchronized AssistCustomerPayEngine getInstance(Context context) {
        if (instance == null) {
            instance = new AssistCustomerPayEngine(context);
        }
        return instance;
    }

    private AssistCustomerPayEngine(Context c) {
        super(c, DB_NAME_SUFFIX);
    }

    public void payCash(Activity caller, AssistPaymentData data) {
        super.payCash(caller, buildServiceEnvironment(data));
    }

    public void payWeb(Activity caller, AssistPaymentData data) {
        payWeb(caller, data, false);
    }

    public void payWeb(Activity caller, AssistPaymentData data, boolean useCamera) {
        super.payWeb(caller, buildServiceEnvironment(data), useCamera);
    }

    @Override
    protected AssistResultProcessor getResultProcessor() {
        return new AssistResultProcessor(
                getContext(),
                new AssistProcessorEnvironment(this, AssistProcessorEnvironment.ServiceMode.CUSTOMER, null, null)
        );
    }

    @Override
    public String getRegistrationId() {
        return getInstInfo().getAppRegId();
    }

    @Override
    protected void onRegistrationSuccess(String registrationID) {
        getInstInfo().setAppRegID(registrationID);
    }

    @Override
    protected RegistrationRequestBuilder getRegRequestBuilder(AssistRegistrationData data) {
        return new CustomerRegistrationRequestBuilder(data);
    }

    private AssistProcessorEnvironment buildServiceEnvironment(AssistPaymentData data) {
        AssistMerchant m = new AssistMerchant(data.getMerchantID());
        return new AssistProcessorEnvironment(
                this,
                AssistProcessorEnvironment.ServiceMode.CUSTOMER,
                m,
                data
        );
    }
}
