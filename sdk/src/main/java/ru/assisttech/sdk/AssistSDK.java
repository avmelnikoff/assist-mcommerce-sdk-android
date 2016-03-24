package ru.assisttech.sdk;

import android.app.Activity;

import ru.assisttech.sdk.engine.AssistCustomerPayEngine;

/**
 *
 */
public class AssistSDK {

	private static final String SDK_VERSION = "1.4.1";

	public static AssistCustomerPayEngine getCustomerPayEngine(Activity activity) {
		return AssistCustomerPayEngine.getInstance(activity.getApplicationContext());
	}

    public static String getSdkVersion() {
        return SDK_VERSION;
    }
}
