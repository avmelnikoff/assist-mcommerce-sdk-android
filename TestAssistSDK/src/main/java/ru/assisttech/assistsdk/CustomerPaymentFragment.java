package ru.assisttech.assistsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

import ru.assisttech.sdk.engine.AssistCustomerPayEngine;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistSDK;

public class CustomerPaymentFragment extends PaymentFragment {

    // Default values
    private static final String DEFAULT_MID = "679471";

    private EditText etMerchantId;
    private EditText etOrderNumber;
    private EditText etOrderAmount;
    private Spinner spCurrency;
    private EditText etOrderComment;
    private EditText etCustomerNumber;
    private EditText etSignature;
    private Spinner spURL;
    private CheckBox cbUseCamera;
    private Button btPay;
    private Button btLog;

    private AssistCustomerPayEngine engine;

    public static String getTitle() {
        return "Customer";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = AssistSDK.getCustomerPayEngine(getActivity());
        engine.setServerURL("https://payments.t.paysecure.ru");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_payment, container, false);

        etMerchantId = (EditText)v.findViewById(R.id.etMerchantID);
        etOrderNumber = (EditText)v.findViewById(R.id.etOrderNumber);
        etOrderAmount = (EditText)v.findViewById(R.id.etOrderAmount);
        spCurrency = (Spinner)v.findViewById(R.id.spCurrency);
        etOrderComment = (EditText)v.findViewById(R.id.etOrderComment);
        etCustomerNumber = (EditText)v.findViewById(R.id.etCustomerNumber);
        etSignature = (EditText) v.findViewById(R.id.etSignature);
        spURL = (Spinner)v.findViewById(R.id.spURL);
        cbUseCamera = (CheckBox)v.findViewById(R.id.cbUseCamera);
        btPay = (Button)v.findViewById(R.id.btPay);
        btLog = (Button)v.findViewById(R.id.btLog);

        // Set defaults
        etMerchantId.setText(DEFAULT_MID);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.currency, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(adapter);

        ArrayAdapter<String> urlsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, urls);
        spURL.setAdapter(urlsAdapter);

        btPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });

        btLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfiguration();
                startActivity(new Intent(getActivity(), TransactionsActivity.class));
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        etOrderNumber.setText(String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    private void startPayment() {

        engine.setServerURL(urls[spURL.getSelectedItemPosition()]);

        data.setMerchantId(etMerchantId.getText().toString());
        data.setOrderNumber(etOrderNumber.getText().toString());
        data.setOrderAmount(etOrderAmount.getText().toString());

        int pos = spCurrency.getSelectedItemPosition();
        AssistPaymentData.Currency[] cur = {
                AssistPaymentData.Currency.RUB,
                AssistPaymentData.Currency.USD,
                AssistPaymentData.Currency.EUR,
                AssistPaymentData.Currency.BYR
        };
        data.setOrderCurrency(cur[pos]);
        data.setOrderComment(etOrderComment.getText().toString());
        data.setCustomerNumber(etCustomerNumber.getText().toString());
        if (etSignature.getText().length() > 0) {
            data.setSignature(etSignature.getText().toString());
        }

        CustomerActivity.setContactData(data);
        CustomerActivity.setCustomerData(data);
        CustomerActivity.setCustomerExData(data);
        SettingsActivity.setSettings(data);
        SettingsActivity.setPaymentMode(data);
        SettingsActivity.setRecurringData(data);

        setConfiguration();

        engine.setEngineListener(this);
        engine.payWeb(getActivity(), data, cbUseCamera.isChecked());
    }

    private void setConfiguration() {
        configuration.setCustomerEngine(engine);
        configuration.setCustomerData(data);
    }
}
