package ru.assisttech.sdk.registration;

public class RegistrationRequestBuilder {

    private static final String ENVELOPE_NAMESPACE = "\"http://schemas.xmlsoap.org/soap/envelope/\"";
    private static final String NAMESPACE = "ns1";
    private static final String REG_NAMESPACE = "\"http://www.paysecure.ru/ws/\"";

    private static final String APPLICATION_NAME = "ApplicationName";
    private static final String APPLICATION_VERSION = "ApplicationVersion";
    private static final String DEVICE_UNIQUE_ID = "DeviceUniqueId";

    private AssistRegistrationData data;

    public RegistrationRequestBuilder(AssistRegistrationData data) {
        this.data = data;
    }

    String buildRequest() {
        StringBuilder request = new StringBuilder();

        //Log.d(TAG, "Prepare request");

        request.append("<s11:Envelope xmlns:s11=" + ENVELOPE_NAMESPACE + ">");
        request.append("<s11:Body>");
        request.append("<" + NAMESPACE + ":getRegistration" + " xmlns:" + NAMESPACE + "=" + REG_NAMESPACE + ">");

        request.append("<" + APPLICATION_NAME + ">");
        request.append(data.getApplicationName());
        request.append("</" + APPLICATION_NAME + ">");

        request.append("<" + APPLICATION_VERSION + ">");
        request.append(data.getAppVersion());
        request.append("</" + APPLICATION_VERSION + ">");

        request.append("<" + DEVICE_UNIQUE_ID + ">");
        request.append(data.getDeviceID());
        request.append("</" + DEVICE_UNIQUE_ID + ">");

        request.append("</" + NAMESPACE + ":getRegistration>");
        request.append("</s11:Body>");
        request.append("</s11:Envelope>");
        //Log.d(TAG, "SOAP Request: " + request.toString());

        return request.toString();
    }
}
