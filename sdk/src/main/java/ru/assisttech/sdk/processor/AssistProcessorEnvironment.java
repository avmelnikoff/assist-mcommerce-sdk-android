package ru.assisttech.sdk.processor;

import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.engine.AssistBasePayEngine;

/**
 * Provides environment for AssistService
 */
public class AssistProcessorEnvironment {

    private AssistBasePayEngine engine; /* pay engine instance that started service */
    private AssistMerchant merchant;    /* Assist registered merchant - money destination */
    private AssistPaymentData data;     /* payment parameters required by Assist system */
    private ServiceMode mode;           /* defines payment request details whether it is sent by MERCHANT or by CUSTOMER application */

    public enum ServiceMode {
        MERCHANT,
        CUSTOMER
    }

    public AssistProcessorEnvironment(AssistBasePayEngine engine,
                                      ServiceMode mode,
                                      AssistMerchant merchant,
                                      AssistPaymentData data) {
        this.engine = engine;
        this.merchant = merchant;
        this.data = data;
        this.mode = mode;
    }

    public AssistBasePayEngine getPayEngine() {
        return engine;
    }

    public AssistMerchant getMerchant() {
        return merchant;
    }

    public AssistPaymentData getData() {
        return data;
    }

    public ServiceMode getMode() {
        return mode;
    }
}
