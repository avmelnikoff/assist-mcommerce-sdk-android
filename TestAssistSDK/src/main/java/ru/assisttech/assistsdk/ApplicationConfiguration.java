package ru.assisttech.assistsdk;

import ru.assisttech.sdk.engine.AssistPayEngine;
import ru.assisttech.sdk.AssistPaymentData;

class ApplicationConfiguration {

    private static ApplicationConfiguration instance;

    private AssistPayEngine payEngine;
    private AssistPaymentData paymentData;
    private String server;
    private boolean useCamera;

    private ApplicationConfiguration() {}

    static ApplicationConfiguration getInstance() {
        if (instance == null) {
            instance = new ApplicationConfiguration();
        }
        return instance;
    }

    void setPayEngine(AssistPayEngine engine) {
        if (payEngine != engine) {
            payEngine = engine;
        }
    }

    void setPaymentData(AssistPaymentData data) {
        if (paymentData != data) {
            paymentData = data;
        }
    }

    AssistPayEngine getPaymentEngine() {
        return payEngine;
    }

    AssistPaymentData getPaymentData() {
        return paymentData;
    }

    boolean isUseCamera() {
        return useCamera;
    }

    void setUseCamera(boolean useCamera) {
        this.useCamera = useCamera;
    }

    String getServer() {
        return server;
    }

    void setServer(String server) {
        this.server = server;
    }
}
