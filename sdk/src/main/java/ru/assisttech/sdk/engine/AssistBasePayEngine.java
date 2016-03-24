package ru.assisttech.sdk.engine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import ru.assisttech.sdk.AssistAddress;
import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.R;
import ru.assisttech.sdk.identification.InstallationInfo;
import ru.assisttech.sdk.identification.SystemInfo;
import ru.assisttech.sdk.network.AssistNetworkEngine;
import ru.assisttech.sdk.registration.AssistRegistrationData;
import ru.assisttech.sdk.registration.AssistRegistrationProvider;
import ru.assisttech.sdk.registration.RegistrationRequestBuilder;
import ru.assisttech.sdk.processor.AssistBaseProcessor;
import ru.assisttech.sdk.processor.AssistPayCashProcessor;
import ru.assisttech.sdk.processor.AssistResultProcessor;
import ru.assisttech.sdk.processor.AssistProcessorEnvironment;
import ru.assisttech.sdk.processor.AssistProcessorListener;
import ru.assisttech.sdk.processor.AssistWebProcessor;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionStorage;
import ru.assisttech.sdk.storage.AssistTransactionStorageImpl;

/**
 * Initiates payment process, checks connection and registration
 */
public abstract class AssistBasePayEngine {
						
	private static final String TAG = "AssistPayEngine";

    private String ServerUrl = AssistAddress.DEFAULT_SERVER;

	private Context appContext;
	private Activity callerActivity;
	private SystemInfo sysInfo;		
	private InstallationInfo instInfo;
	private AssistNetworkEngine netEngine;

    protected AssistBaseProcessor processor;
	private AssistTransactionStorage storage;

	private PayEngineListener engineListener;
	
	private ProgressDialog pd;
    private boolean connectionChecked;
    private boolean finished;

    AssistBasePayEngine(Context c, String dbNameSuffix) {
        appContext = c.getApplicationContext();
        netEngine = new AssistNetworkEngine(appContext);
        storage = new AssistTransactionStorageImpl(appContext, dbNameSuffix);
        sysInfo = SystemInfo.getInstance(appContext);
        instInfo = InstallationInfo.getInstance(appContext);
    }

	public void setEngineListener(PayEngineListener listener) {
		engineListener = listener;
	}

    public PayEngineListener getEngineListener() {
        return engineListener;
    }

    public void addNetworkCertificate(Certificate cert) {
        netEngine.addCertificate(cert);
    }

    public void setServerURL(String url) {
        if (!TextUtils.isEmpty(url)) {
            try {
                String protocol = new URL(url).getProtocol();
                if (TextUtils.isEmpty(protocol)) {
                    ServerUrl = "https://" + url;
                } else {
                    ServerUrl = url;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public AssistTransactionStorage transactionStorage() {
        return storage;
    }

    public SystemInfo getSystemInfo() {
        return sysInfo;
    }

    public InstallationInfo getInstInfo() {
        return instInfo;
    }

    /**
     * Save customer signature for current transaction into DB
     * @param id transaction ID {@link AssistTransaction#getId()}
     * @param signature user signature as array of points
     */
    public void setUserSignature(long id, byte[] signature) {
        storage.updateTransactionSignature(id, signature);
    }

	public void payCash(Activity caller, AssistProcessorEnvironment environment) {

        Log.e(TAG, "Cash payment is not implemented");

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistMerchant m = environment.getMerchant();
            AssistPaymentData data = environment.getData();
            AssistTransaction t = createTransaction(m.getID(), data, AssistTransaction.PaymentMethod.CASH);

            processor = new AssistPayCashProcessor(getContext(), environment);
            processor.setListener(new PayCashProcessorListener());
            processor.setTransaction(t);

            startPayment();
        }
	}

    /**
     * Web based payment process
     * Uses device camera for card data input
     */
    public void payWeb(Activity caller, AssistProcessorEnvironment environment, boolean useCamera) {

        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);

            AssistMerchant m = environment.getMerchant();
            AssistPaymentData data = environment.getData();
            data.setMobileDevice("5");  // Tells Assist server what web pages to show

            AssistTransaction t = createTransaction(
                        m.getID(),
                        data,
                        useCamera ? AssistTransaction.PaymentMethod.CARD_PHOTO_SCAN :
                                    AssistTransaction.PaymentMethod.CARD_MANUAL
            );

            processor = new AssistWebProcessor(getContext(), environment, useCamera);
            processor.setNetEngine(netEngine);
            processor.setURL(getWebServiceUrl());
            processor.setListener(new WebProcessorListener());
            processor.setTransaction(t);

            checkRegistration();
        }
    }

    /**
     * Gets result for certain payment
     * @param id transaction ID {@link AssistTransaction#getId()}
     */
	public void getOrderResult(Activity caller, long id) {
        if (isEngineReady()) {
            saveCallerActivity(caller);
            setFinished(false);
            processor = initResultProcessor(id);
            checkRegistration();
        }
	}

    public void stopPayment(Activity caller) {
        setFinished(true);
        if ((processor != null) && (processor.isRunning())) {
            processor.stop();
            getEngineListener().onCanceled(caller, processor.getTransaction());
            processor = null;
        }
    }

    /**
     * Method implemented in successors because there are
     * differences between customer and merchant registration ID.
     */
    public abstract String getRegistrationId();

    /**
     * Checks application registration in Assist Payment System
     */
    protected void checkRegistration() {
        /* Check network connection first */
        if (!connectionChecked) {
            checkNetworkConnection();
            return;
        }

        if (getRegistrationId() == null) {
            startRegistration();
        } else {
            startPayment();
        }
    }

    /**
     * Application installation registration in Assist Payment System
     */
    protected void startRegistration() {

        AssistRegistrationData regData = new AssistRegistrationData();
        regData.setApplicationName(instInfo.appName());
        regData.setAppVersion(instInfo.versionName());
        regData.setDerviceID(sysInfo.fingerprint());

        AssistRegistrationProvider rp = new AssistRegistrationProvider(getContext());
        rp.setNetworkEngine(netEngine);
        rp.setURL(getRegistrationUrl());
        rp.setResutListener(new RegistrationResultListener());
        rp.register(getRegRequestBuilder(regData));
    }

    /**
     * Method implemented in successors because there are
     * differences between customer and merchant mode registration.
     */
    protected abstract RegistrationRequestBuilder getRegRequestBuilder(AssistRegistrationData data);

    /**
     * Method implemented in successors because there are
     * differences between customer and merchant registration ID.
     */
    protected abstract void onRegistrationSuccess(String registrationID);


    /**
     * HTTPS connection's certificate check procedure
     */
    protected void checkNetworkConnection() {
        Log.d(TAG, "Check URL: " + ServerUrl);
        try {
            URL u = new URL(ServerUrl + AssistAddress.WEB_SERVICE);
            showProgressDialog(getCallerActivity(), R.string.connection_check);
            netEngine.checkHTTPSConnection(u, new ConnectionCheckListener());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


	protected AssistTransaction createTransaction(String merchantID, AssistPaymentData data, AssistTransaction.PaymentMethod method) {
        AssistTransaction t = new AssistTransaction();
        t.setMerchantID(merchantID);
        t.setOrderAmount(data.getFields().get(FieldName.OrderAmount));
        t.setOrderComment(data.getFields().get(FieldName.OrderComment));
        t.setOrderCurrency(AssistPaymentData.Currency.valueOf(data.getFields().get(FieldName.OrderCurrency)));
        t.setOrderNumber(data.getFields().get(FieldName.OrderNumber));
        if (data.getOrderItems() != null) {
            t.setOrderItems(data.getOrderItems());
        }
        t.setPaymentMethod(method);
        /* Check if transaction requires user signature */
        if (data.getFields().containsKey(FieldName.PaymentMode)) {
            String mode = data.getFields().get(FieldName.PaymentMode);
            t.setRequireUserSignature(AssistPaymentData.PaymentMode.POSKeyEntry.equals(mode));
        }
        return t;
	}

    protected void startProcessor() {
		processor.start(getCallerActivity());
	}

    /**
     * Transaction saving and payment beginning
     */
    protected void startPayment() {
        Log.d(TAG, "startPayment()");
        if (!processor.getTransaction().isStored()) {
            storage.add(processor.getTransaction()); // TODO: Check return value (whether transaction added successfully or not)
        }
        startProcessor();
    }


    /**
     * Internal method to get payment result after payment process completion.
     * Does not check application registration
     * @param id transaction id {@link AssistTransaction#getId()}
     */
    protected void getResult(long id) {
        processor = initResultProcessor(id);
        startProcessor();
    }

    /**
     * Method implemented in successors because there are differences
     * whether getting result in customer or merchant modes
     */
    protected abstract AssistResultProcessor getResultProcessor();

    protected ResultProcessorListener getResultProcessorListener() {
        return new ResultProcessorListener();
    }

    /**
     * @param id transaction id {@link AssistTransaction#getId()}
     * @return {@link AssistResultProcessor}
     */
    protected AssistResultProcessor initResultProcessor(long id) {
        AssistResultProcessor rp = getResultProcessor();
        rp.setNetEngine(netEngine);
        rp.setURL(getGetOrderStatusUrl());
        rp.setListener(getResultProcessorListener());
        rp.setTransaction(getTransaction(id));
        return rp;
    }

    /**
     * Obtain transaction form DB
     * @param id transaction id {@link AssistTransaction#getId()}
     */
    protected AssistTransaction getTransaction(long id) {
        return storage.getTransaction(id);
    }

    /**
     * Update transaction in DB with new {@link AssistResult}
     * @param id ID of transaction to cancel {@link AssistTransaction#getId()}
     * @param result result of transaction {@link AssistResult}
     */
	protected void updateTransaction(long id, AssistResult result) {
		storage.updateTransactionResult(id, result);
	}


    protected void engineInitializationFailed(String info) {
		processor = null;
        if (!isFinished()) {
            getEngineListener().onFailure(getCallerActivity(), info);
        }
	}

    protected Context getContext() {
        return appContext;
    }

    protected String getServerUrl() {
        return ServerUrl;
    }

    protected AssistNetworkEngine getNetEngine() {
        return netEngine;
    }

    protected String getRegistrationUrl() {
        return getServerUrl() + AssistAddress.REGISTRATION_SERVICE;
    }

    protected String getWebServiceUrl() {
        return getServerUrl() + AssistAddress.WEB_SERVICE;
    }

    protected String getGetOrderStatusUrl() {
        return getServerUrl() + AssistAddress.GET_ORDER_STATUS_SERVICE;
    }

    protected boolean isEngineReady() {
        return processor == null || !processor.isRunning();
    }

    protected void saveCallerActivity(Activity caller) {
        callerActivity = caller;
    }

    protected Activity getCallerActivity() {
        return callerActivity;
    }

    protected boolean isFinished() {
        return finished;
    }

    protected void setFinished(boolean value) {
        this.finished = value;
    }

    protected void showProgressDialog(Activity activity, int messageResId) {
		if (pd != null) {
			return;
		}
		pd = new ProgressDialog(activity);
		pd.setCanceledOnTouchOutside(false);
        pd.setMessage(appContext.getString(messageResId));
		pd.show();
	}

    protected void closeProgressDialog() {
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

    /**
     * Connection check result processing
     */
    protected class ConnectionCheckListener implements AssistNetworkEngine.ConnectionCheckListener {

        @Override
        public void onConnectionSuccess() {
            Log.d(TAG, "Connection check success");
            closeProgressDialog();
            connectionChecked = true;
            checkRegistration();
        }

        @Override
        public void onConnectionFailure(String info) {
            Log.d(TAG, "Connection check error. " + info);
            closeProgressDialog();
            connectionChecked = true;
            engineInitializationFailed(getContext().getString(R.string.connection_error));
        }
    }

    /**
     * Registration result processing
     */
    protected class RegistrationResultListener implements AssistRegistrationProvider.RegistrationResultListener {

        @Override
        public void onRegistrationOk(String registrationID) {
            Log.d(TAG, "onRegistrationOk(): " + registrationID);
            onRegistrationSuccess(registrationID);
            startPayment();
        }

        @Override
        public void onRegistrationError(String faultCode, String faultString) {
            Log.d(TAG, "onRegistrationError(): " + faultCode + ": " + faultString);
            engineInitializationFailed(getContext().getString(R.string.registration_error));
        }
    }

    /**
     * BaseProcessorListener callback listener {@link AssistPayCashProcessor}
     */
    protected abstract class BaseProcessorListener implements AssistProcessorListener {

        @Override
        public void onNetworkError(long id, String message) {
            Log.d(TAG, "PayCashProcessorListener.onNetworkError() " + message);
            getEngineListener().onNetworkError(getCallerActivity(), message);
        }

        @Override
        public void onTerminated(long id) {
            Log.d(TAG, "PayCashProcessorListener.onTerminated()");
            getEngineListener().onCanceled(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onActivityCreated(Activity newActivity) {
            Log.d(TAG, "PayCashProcessorListener.onActivityCreated()");
            if (newActivity != null) {
                saveCallerActivity(newActivity);
            }
        }
    }

    /**
     * PayCashProcessor callback listener {@link ru.assisttech.sdk.engine.AssistBasePayEngine.BaseProcessorListener}
     */
    protected class PayCashProcessorListener extends BaseProcessorListener {

        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "PayCashProcessorListener.onFinished() id = : " + String.valueOf(id) + "; " + result.getExtra());
            if (!isFinished()) {
                updateTransaction(id, result);
                getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
            }
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "PayCashProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * WebProcessor callback listener {@link ru.assisttech.sdk.engine.AssistBasePayEngine.BaseProcessorListener}
     */
    protected class WebProcessorListener extends BaseProcessorListener {

        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "WebProcessorListener.onFinished() id = : " + String.valueOf(id) + "; " + result.getExtra());
            if (!isFinished()) {
                getResult(id);
            }
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "WebProcessorListener.onError() " + message);
            transactionStorage().deleteTransaction(id);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }

    /**
     * ResultProcessor callback listener {@link ru.assisttech.sdk.engine.AssistBasePayEngine.BaseProcessorListener}
     */
    protected class ResultProcessorListener extends BaseProcessorListener {

        @Override
        public void onFinished(long id, AssistResult result) {
            Log.d(TAG, "ResultProcessorListener.onFinished() " + result.getOrderState());
            updateTransaction(id, result);
            getEngineListener().onFinished(getCallerActivity(), getTransaction(id));
        }

        @Override
        public void onError(long id, String message) {
            Log.d(TAG, "ResultProcessorListener.onError() " + message);
            getEngineListener().onFailure(getCallerActivity(), message);
        }
    }
}
