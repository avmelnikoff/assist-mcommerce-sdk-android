package ru.assisttech.sdk.registration;

/**
 * Builds request for customer registration
 */
public class CustomerRegistrationRequestBuilder extends RegistrationRequestBuilder {

    public CustomerRegistrationRequestBuilder(AssistRegistrationData data) {
        super(data);
    }

    @Override
    void modifyRequest(AssistRegistrationData data, StringBuilder request) {
        // Nothing to modify
    }
}
