package ru.assisttech.assistsdk;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.util.Locale;

import ru.assisttech.assistsdk.androidpay.Constants;
import ru.assisttech.assistsdk.androidpay.ItemInfo;
import ru.assisttech.assistsdk.androidpay.WalletUtil;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.engine.AssistPayEngine;
import ru.assisttech.sdk.engine.PayEngineListener;
import ru.assisttech.sdk.storage.AssistTransaction;

/**
 * Экран подтверждения данных платежа и выбора способа оплаты web или AndroidPay если доступен
 */
public class ConfirmationActivity extends FragmentActivity {

    public static final String TAG = "ConfirmationActivity";

    private static final int REQUEST_CODE_MASKED_WALLET = 1001;
    private static final int REQUEST_CODE_CHANGE_MASKED_WALLET = 1002;
    /**
     * Request code used when loading a full wallet. Only use this request code when calling
     * {@link Wallet#loadFullWallet(GoogleApiClient, FullWalletRequest, int)}.
     */
    public static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1004;

    // Google Wallet
    private SupportWalletFragment mWalletFragment;
    private GoogleApiClient googleApiClient;
    private MaskedWallet mMaskedWallet;
    private ItemInfo mAndroidPayItemInfo;

    private ApplicationConfiguration configuration;
    private AssistPayEngine engine;
    private AssistPaymentData data;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        TextView tvServer = (TextView) findViewById(R.id.tvServer);
        TextView tvMerchant = (TextView) findViewById(R.id.tvMerchant);
        TextView tvOrderNumber = (TextView) findViewById(R.id.tvOrderNumber);
        TextView tvOrderAmount = (TextView) findViewById(R.id.tvOrderAmount);
        TextView tvOrderComment = (TextView) findViewById(R.id.tvOrderComment);
        Button btPayWeb = (Button) findViewById(R.id.btPay);

        configuration = ApplicationConfiguration.getInstance();
        engine = AssistSDK.getPayEngine(this);
        data = configuration.getPaymentData();

        tvServer.setText(configuration.getServer());
        tvMerchant.setText(data.getMerchantID() + " [" + data.getLogin() + ":" + data.getPassword() + "]");
        tvOrderNumber.setText(data.getFields().get(FieldName.OrderNumber));
        tvOrderAmount.setText(data.getFields().get(FieldName.OrderAmount) + " " + data.getFields().get(FieldName.OrderCurrency));
        tvOrderComment.setText(data.getFields().get(FieldName.OrderComment));

        String itemName = String.format(Locale.US, getString(R.string.item_name_format), data.getFields().get(FieldName.OrderNumber));

        mAndroidPayItemInfo = new ItemInfo.Builder()
                .setMerchantName(data.getMerchantID())
                .setSellerData(data.getMerchantID())
                .setName(itemName)
                .setPrice(data.getFields().get(FieldName.OrderAmount))
                .setCurrencyCode(data.getFields().get(FieldName.OrderCurrency))
                .build();

        btPayWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payWeb();
            }
        });

        /*
         * Подготовка компонент для работы с Android Pay
         */
        // [START basic_google_api_client]
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder().setEnvironment(Constants.WALLET_ENVIRONMENT).build())
                .enableAutoManage(this, new ConnectionFailedListener())
                .build();
        // [END basic_google_api_client]

        // Check if user is ready to use Android Pay
        // [START is_ready_to_pay]
        Wallet.Payments.isReadyToPay(googleApiClient).setResultCallback(
                new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {
                        if (booleanResult.getStatus().isSuccess()) {
                            if (booleanResult.getValue()) {
                                // Show Android Pay buttons and hide regular checkout button
                                // [START_EXCLUDE]
                                Log.d(TAG, "isReadyToPay:true");
                                createAndAddWalletFragment();
                                // [END_EXCLUDE]
                            } else {
                                // Hide Android Pay buttons, show a message that Android Pay
                                // cannot be used yet, and display a traditional checkout button
                                // [START_EXCLUDE]
                                Log.d(TAG, "isReadyToPay:false:" + booleanResult.getStatus());
                                findViewById(R.id.android_pay_message).setVisibility(View.VISIBLE);
                                findViewById(R.id.dynamic_wallet_button_fragment).setVisibility(View.GONE);
                                // [END_EXCLUDE]
                            }
                        } else {
                            // Error making isReadyToPay call
                            Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                        }
                    }
                }
        );
        // [END is_ready_to_pay]
    }

    /**
     * If the confirmation page encounters an error it can't handle, it will send the customer back
     * to this page.  The intent should include the error code as an {@code int} in the field
     * {@link WalletConstants#EXTRA_ERROR_CODE}.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(WalletConstants.EXTRA_ERROR_CODE)) {
            int errorCode = intent.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, 0);
            handleError(errorCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // no need to show login menu on confirmation screen
        return false;
    }

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode: " + requestCode + "; resultCode: " + requestCode);
        // retrieve the error code, if available
        int errorCode = -1;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        }
        Log.d(TAG, "errorCode: " + errorCode);
        switch (requestCode) {

            case REQUEST_CODE_MASKED_WALLET:
                Log.d(TAG, "REQUEST_CODE_MASKED_WALLET");
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            launchConfirmationPage(maskedWallet);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        handleError(errorCode);
                        break;
                }
                break;

            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                Log.d(TAG, "REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET");
                hideProgress();
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null && data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                            FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                            // the full wallet can now be used to process the customer's payment
                            // send the wallet info up to server to process, and to get the result
                            // for sending a transaction status
                            fetchTransactionStatus(fullWallet);
                        } else if (data != null && data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                            // re-launch the activity with new masked wallet information
                            mMaskedWallet =  data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            createAndAddWalletFragment();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // nothing to do here
                        break;
                    default:
                        handleError(errorCode);
                        break;
                }
                break;

            case REQUEST_CODE_CHANGE_MASKED_WALLET:
                Log.d(TAG, "REQUEST_CODE_CHANGE_MASKED_WALLET");
                if (resultCode == Activity.RESULT_OK && data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                    mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                    mWalletFragment.updateMaskedWallet(mMaskedWallet);
                }
                // you may also want to use the new masked wallet data here, say to recalculate
                // shipping or taxes if shipping address changed
                break;

            case WalletConstants.RESULT_ERROR:
                Log.d(TAG, "WalletConstants.RESULT_ERROR");
                handleError(errorCode);
                break;
            default:
                Log.d(TAG, "default");
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    // [END on_activity_result]


    private void createAndAddWalletFragment() {
        // [START fragment_style_and_options]
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                .setBuyButtonAppearance(WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK)
                .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(Constants.WALLET_ENVIRONMENT)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();
        mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);
        // [END fragment_style_and_options]

        // Now initialize the Wallet Fragment
        String accountName = null;

        // Direct integration
        MaskedWalletRequest maskedWalletRequest = WalletUtil.createMaskedWalletRequest(
                mAndroidPayItemInfo,
                getString(R.string.public_key)
        );
        // [START params_builder]
        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(REQUEST_CODE_MASKED_WALLET)
                .setAccountName(accountName);

        mWalletFragment.initialize(startParamsBuilder.build());

        // add Wallet fragment to the UI
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dynamic_wallet_button_fragment, mWalletFragment)
                .commit();

        findViewById(R.id.dynamic_wallet_button_fragment).setVisibility(View.VISIBLE);
        // [END params_builder]
    }

    private void payWeb() {
        engine.setEngineListener(new PEngineListener());
        engine.payWeb(this, data, configuration.isUseCamera());
    }

    private void payToken(PaymentMethodToken token) {
        engine.setEngineListener(new PEngineListener());
        data.setPaymentToken(token.getToken());
        engine.setEngineListener(new PEngineListener());
        engine.payToken(this, data);
    }

    protected void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                Toast.makeText(this, "Превышен лимит расходов", Toast.LENGTH_LONG).show();
                break;
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
            case WalletConstants.ERROR_CODE_UNKNOWN:
            default:
                // unrecoverable error
                String errorMessage = "Кошелек Google недоступен\nКод ошибки: " + errorCode;
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void launchConfirmationPage(MaskedWallet maskedWallet) {
        showProgress();
        mMaskedWallet = maskedWallet;
        getFullWallet();
    }

    private void getFullWallet() {
        FullWalletRequest fullWalletRequest = WalletUtil.createFullWalletRequest(mAndroidPayItemInfo, mMaskedWallet.getGoogleTransactionId());
        // [START load_full_wallet]
        Wallet.Payments.loadFullWallet(googleApiClient, fullWalletRequest, REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
        // [END load_full_wallet]
    }

    /**
     * Here the client should connect to their server, process the credit card/instrument
     * and get back a status indicating whether charging the card was successful or not
     */
    private void fetchTransactionStatus(FullWallet fullWallet) {
        Log.d(TAG, "fetchTransactionStatus");
        Log.d(TAG, "FullWallet: " + fullWallet);

        showProgress();
        // Log payment method token, if it exists. This token will either be a direct integration token
        PaymentMethodToken token = fullWallet.getPaymentMethodToken();
        if (token != null) {
            // getToken returns a JSON object as a String
            //
            // For a Direct Integration token, the object will have the following format:
            // {
            //    encryptedMessage: <string,base64>
            //    ephemeralPublicKey: <string,base64>
            //    tag: <string,base64>
            // }
            // See the Android Pay documentation for more information on how to decrypt the token.

            // Pretty-print the token to LogCat (newlines replaced with spaces).
            Log.d(TAG, "PaymentMethodToken:" + token.getToken().replace('\n', ' '));
            payToken(token);
        }
        // Send details such as fullWallet.getProxyCard() or fullWallet.getBillingAddress()
        // to your server and get back success or failure.
        // Intent intent = new Intent(getActivity(), MainActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra(Constants.EXTRA_FULL_WALLET, fullWallet);
        // startActivity(intent);
    }

    private void showAlertDialog(Activity activity, String dlgTitle, String dlgMessage) {
        Log.d(TAG, "Show alert: " + dlgTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dlgTitle);
        builder.setMessage(dlgMessage);
        builder.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    void showProgress() {
        showProgress(null);
    }

    void showProgress(String message) {
        // Construct a progress dialog to prevent user from actions until connection is finished.
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            if (TextUtils.isEmpty(message)) {
                progressDialog.setMessage(getString(R.string.please_wait));
            } else {
                progressDialog.setMessage(message);
            }
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            progressDialog.setMessage(message);
        }
    }

    void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Слушатель результата проведения оплаты
     */
    private class PEngineListener implements PayEngineListener {
        @Override
        public void onFinished(Activity activity, AssistTransaction assistTransaction) {
            hideProgress();
            Intent intent = new Intent(ConfirmationActivity.this, ViewResultActivity.class);
            intent.putExtra(ViewResultActivity.TRANSACTION_ID_EXTRA, assistTransaction.getId());
            startActivity(intent);
        }
        @Override
        public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
            hideProgress();

        }
        @Override
        public void onFailure(Activity activity, String info) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_error), info);
        }
        @Override
        public void onNetworkError(Activity activity, String message) {
            hideProgress();
            showAlertDialog(activity, getString(R.string.alert_dlg_title_network_error), message);
        }
    }

    /**
     * Слушатель ошибки подключения к сервисам Google
     */
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e(TAG, "onConnectionFailed:" + connectionResult.getErrorMessage());
            Toast.makeText(ConfirmationActivity.this, "Google Play Services error", Toast.LENGTH_SHORT).show();
        }
    }
}
