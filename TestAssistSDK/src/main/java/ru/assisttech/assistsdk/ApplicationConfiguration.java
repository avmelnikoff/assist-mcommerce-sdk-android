package ru.assisttech.assistsdk;

import ru.assisttech.sdk.engine.AssistBasePayEngine;
import ru.assisttech.sdk.engine.AssistCustomerPayEngine;
import ru.assisttech.sdk.AssistPaymentData;

public class ApplicationConfiguration {

    private static ApplicationConfiguration instance;

    private AssistCustomerPayEngine customerEngine;
    private AssistPaymentData customerData;

    private ActiveEngine activeEngine;

    private enum ActiveEngine {
        CUSTOMER
    }

    private ApplicationConfiguration() {}

    public static ApplicationConfiguration getInstance() {
        if (instance == null) {
            instance = new ApplicationConfiguration();
        }
        return instance;
    }

    public void setCustomerEngine(AssistCustomerPayEngine engine) {
        if (customerEngine != engine) {
            customerEngine = engine;
        }
        activeEngine = ActiveEngine.CUSTOMER;
    }

    public void setCustomerData(AssistPaymentData data) {
        if (customerData != data) {
            customerData = data;
        }
    }

    public AssistBasePayEngine getPaymentEngine() {
        switch (activeEngine) {
            case CUSTOMER:
                return customerEngine;
            default:
                return null;
        }
    }

    public AssistPaymentData getPaymentData() {
        switch (activeEngine) {
            case CUSTOMER:
                return customerData;
            default:
                return null;
        }
    }
}
