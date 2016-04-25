package ru.assisttech.assistsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.engine.PayEngineListener;
import ru.assisttech.sdk.storage.AssistTransaction;


public abstract class PaymentFragment extends Fragment implements PayEngineListener {

    protected ApplicationConfiguration configuration;
    protected AssistPaymentData data;

    protected String[] urls = {
            "https://payments.t.paysecure.ru",
            "https://test.paysec.by",
            "https://payments.paysec.by",
            "https://test.paysecure.ru",
            "https://payments.paysecure.ru"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        initPaymentData();
        configuration = ApplicationConfiguration.getInstance();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_customer:
                startActivity(new Intent(getActivity(), CustomerActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("TrulyRandom")
    protected void initPaymentData() {
        data = new AssistPaymentData();
        data.clear();
        data.setYMPayment(false);
        data.setWMPayment(false);
        data.setQIWIPayment(false);
        data.setQIWIMegafonPayment(false);
        data.setQIWIMtsPayment(false);
        data.setQIWIBeelinePayment(false);

        // Generate key
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair keyPair = kpg.genKeyPair();
            data.setPrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onFinished(Activity activity, AssistTransaction assistTransaction) {
        Intent intent = new Intent(getActivity(), ViewResultActivity.class);
        intent.putExtra(ViewResultActivity.TRANSACTION_ID_EXTRA, assistTransaction.getId());
        startActivity(intent);
    }

    @Override
    public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
        toMainActivity();
    }

    @Override
    public void onFailure(Activity activity, String info) {
        showAlertDialog(activity, getString(R.string.alert_dlg_title_error), info);
    }

    @Override
    public void onNetworkError(Activity activity, String message) {
        showAlertDialog(activity, getString(R.string.alert_dlg_title_network_error), message);
    }

    private void toMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void showAlertDialog(Activity activity, String dlgTitle, String dlgMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dlgTitle);
        builder.setMessage(dlgMessage);
        builder.setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                toMainActivity();
            }
        });
        builder.show();
    }
}
