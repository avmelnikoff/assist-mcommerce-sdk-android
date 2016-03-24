package ru.assisttech.sdk.registration;

public class AssistRegistrationData {

    private String appName;
    private String appVersion;
    private String deviceID;
    private String merchantID;

    public void setApplicationName(String name) {
        appName = name;
    }

    public String getApplicationName() {
        return appName;
    }

    public void setAppVersion(String version) {
        appVersion = version;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setDerviceID(String id) {
        deviceID = id;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setMerchantID(String id) {
        merchantID = id;
    }

    public String getMerchantID() {
        return merchantID;
    }
}
