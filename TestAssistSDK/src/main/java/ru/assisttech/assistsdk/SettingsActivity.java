package ru.assisttech.assistsdk;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import ru.assisttech.sdk.AssistPaymentData;

public class SettingsActivity extends UpButtonActivity {

	static AssistPaymentData.Lang lang = AssistPaymentData.Lang.RU;

    static Boolean ymPayment;
    static Boolean wmPayment;
    static Boolean qiwiPayment;
    static Boolean qiwiMtsPayment;
    static Boolean qiwiMegafonPayment;
    static Boolean qiwiBeelinePayment;

    static Boolean recurringIndicator;
    static String minAmount;
    static String maxAmount;
    static Integer period;
    static String maxDate;

    private Spinner spLang;

    private CheckBox chYMPayment;
    private CheckBox chWMPayment;
    private CheckBox chQIWIPayment;
    private CheckBox chQIWIMTSPayment;
    private CheckBox chQIWIMegafonPayment;
    private CheckBox chQIWIBeelinePayment;

    private CheckBox chRecurringPayment;
    private EditText etRecMinAmt;
    private EditText etRecMaxAmt;
    private EditText etRecPeriod;
    private EditText etRecMaxDate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
        collectData();
	}

    public static void setSettings(AssistPaymentData data) {
        data.setLanguage(lang);
    }

    public static void setRecurringData(AssistPaymentData params) {
        if(recurringIndicator != null) params.setRecurringIndicator(recurringIndicator);
        if(minAmount != null) params.setRecurringMinAmount(minAmount);
        if(maxAmount != null) params.setRecurringMaxAmount(maxAmount);
        if(period != null) params.setRecurringPeriod(period);
        if(maxDate != null) params.setRecurringMaxDate(maxDate);
    }

    public static void setPaymentMode(AssistPaymentData params) {
        if(ymPayment != null) params.setYMPayment(ymPayment);
        if(wmPayment != null) params.setWMPayment(wmPayment);
        if(qiwiPayment != null) params.setQIWIPayment(qiwiPayment);
        if(qiwiMtsPayment != null) params.setQIWIMtsPayment(qiwiMtsPayment);
        if(qiwiMegafonPayment != null) params.setQIWIMegafonPayment(qiwiMegafonPayment);
        if(qiwiBeelinePayment != null) params.setQIWIBeelinePayment(qiwiBeelinePayment);
    }

    private void initUI() {
        spLang = (Spinner) findViewById(R.id.spLanguage);

        chYMPayment = (CheckBox) findViewById(R.id.chYMPayment);
        chWMPayment = (CheckBox) findViewById(R.id.chWMPayment);
        chQIWIPayment = (CheckBox) findViewById(R.id.chQIWIPayment);
        chQIWIMTSPayment = (CheckBox) findViewById(R.id.chQIWIMtsPayment);
        chQIWIMegafonPayment = (CheckBox) findViewById(R.id.chQIWIMegafonPayment);
        chQIWIBeelinePayment = (CheckBox) findViewById(R.id.chQIWIBeelinePayment);

        chRecurringPayment = (CheckBox) findViewById(R.id.chRecurringIndicator);
        etRecMinAmt = (EditText) findViewById(R.id.etRecMinAmt);
        etRecMaxAmt = (EditText) findViewById(R.id.etRecMaxAmt);
        etRecPeriod = (EditText) findViewById(R.id.etRecPeriod);
        etRecMaxDate = (EditText) findViewById(R.id.etRecMaxDate);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLang.setAdapter(adapter);

        /* Language */
        switch(lang) {
            case EN:
                spLang.setSelection(1);
                break;
            case RU:
                spLang.setSelection(0);
                break;
            case UK:
                spLang.setSelection(2);
                break;
        }

        /* PaymentMode */
        if(ymPayment != null) {
            chYMPayment.setChecked(ymPayment);
        }
        if(wmPayment != null) {
            chWMPayment.setChecked(wmPayment);
        }
        if(qiwiPayment != null) {
            chQIWIPayment.setChecked(qiwiPayment);
        }
        if(qiwiMtsPayment != null) {
            chQIWIMTSPayment.setChecked(qiwiMtsPayment);
        }
        if(qiwiMegafonPayment != null) {
            chQIWIMegafonPayment.setChecked(qiwiMegafonPayment);
        }
        if(qiwiBeelinePayment != null) {
            chQIWIBeelinePayment.setChecked(qiwiBeelinePayment);
        }

        /* Recurring */
        if(recurringIndicator != null) {
            chRecurringPayment.setChecked(recurringIndicator);
        }
        if(minAmount != null) {
            etRecMinAmt.setText(minAmount);
        }
        if(maxAmount != null) {
            etRecMaxAmt.setText(maxAmount);
        }
        if(period != null) {
            etRecPeriod.setText(period);
        }
        if(maxDate != null) {
            etRecMaxDate.setText(maxDate);
        }
    }

    private void collectData() {

        /* Language */
        int pos = spLang.getSelectedItemPosition();
        switch(pos) {
            case 0:
                lang = AssistPaymentData.Lang.RU;
                break;
            case 1:
                lang = AssistPaymentData.Lang.EN;
                break;
            case 2:
                lang = AssistPaymentData.Lang.UK;
                break;
        }

        /* PaymentMode */
        ymPayment = chYMPayment.isChecked();
        wmPayment = chWMPayment.isChecked();
        qiwiPayment = chQIWIPayment.isChecked();
        qiwiMtsPayment = chQIWIMTSPayment.isChecked();
        qiwiMegafonPayment = chQIWIMegafonPayment.isChecked();
        qiwiBeelinePayment = chQIWIBeelinePayment.isChecked();

        /* Recurring */
        recurringIndicator = chRecurringPayment.isChecked();
        minAmount = etRecMinAmt.getText().toString();
        maxAmount = etRecMaxAmt.getText().toString();
        period = Integer.getInteger(etRecPeriod.getText().toString());
        maxDate = etRecMaxDate.getText().toString();
    }
}
