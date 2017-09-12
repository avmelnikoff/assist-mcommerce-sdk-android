package ru.assisttech.assistsdk;

import android.os.Bundle;
import android.widget.EditText;

import ru.assisttech.sdk.AssistPaymentData;

public class CustomerActivity extends UpButtonActivity {

    static String lastName = "Michael";
    static String firstName = "Shumakher";
    static String middleName = "Alexandrovich";

    static String email = "shumka@customs.ps";
    static String address;
    static String homePhone;
    static String workPhone;
    static String mobilePhone;
    static String fax;

    static String country;
    static String state;
    static String city;
    static String zip;

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etMiddleName;

    private EditText etEmail;
    private EditText etAddress;
    private EditText etHomePhone;
    private EditText etWorkPhone;
    private EditText etMobilePhone;
    private EditText etFax;

    private EditText etCountry;
    private EditText etState;
    private EditText etCity;
    private EditText etZip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        collectData();
    }

    public static void setCustomerData(AssistPaymentData params) {
        if (lastName != null) params.setLastname(lastName);
        if (firstName != null) params.setFirstname(firstName);
        if (middleName != null) params.setMiddlename(middleName);
    }

    public static void setContactData(AssistPaymentData params) {
        if (email != null) params.setEmail(email);
        if (address != null) params.setAddress(address);
        if (homePhone != null) params.setHomePhone(homePhone);
        if (workPhone != null) params.setWorkPhone(workPhone);
        if (mobilePhone != null) params.setMobilePhone(mobilePhone);
        if (fax != null) params.setFax(fax);
    }

    public static void setCustomerExData(AssistPaymentData params) {
        if (country != null) params.setCountry(country);
        if (state != null) params.setState(state);
        if (city != null) params.setCity(city);
        if (zip != null) params.setZip(zip);
    }

    private void collectData() {
        lastName = etLastName.getText().toString();
        firstName = etFirstName.getText().toString();
        middleName = etMiddleName.getText().toString();

        email = etEmail.getText().toString();
        address = etAddress.getText().toString();
        homePhone = etHomePhone.getText().toString();
        workPhone = etWorkPhone.getText().toString();
        mobilePhone = etMobilePhone.getText().toString();
        fax = etFax.getText().toString();

        country = etCountry.getText().toString();
        state = etState.getText().toString();
        city = etCity.getText().toString();
        zip = etZip.getText().toString();
    }

    private void initUI() {
        etLastName = (EditText) findViewById(R.id.etLastname);
        etFirstName = (EditText) findViewById(R.id.etFirstname);
        etMiddleName = (EditText) findViewById(R.id.etMiddlename);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etHomePhone = (EditText) findViewById(R.id.etHomePhone);
        etWorkPhone = (EditText) findViewById(R.id.etWorkPhone);
        etMobilePhone = (EditText) findViewById(R.id.etMobilePhone);
        etFax = (EditText) findViewById(R.id.etFax);

        etCountry = (EditText) findViewById(R.id.etCountry);
        etState = (EditText) findViewById(R.id.etState);
        etCity = (EditText) findViewById(R.id.etCity);
        etZip = (EditText) findViewById(R.id.etZip);

        if (lastName != null) {
            etLastName.setText(lastName);
        }

        if (firstName != null) {
            etFirstName.setText(firstName);
        }

        if (middleName != null) {
            etMiddleName.setText(middleName);
        }

        if (email != null) {
            etEmail.setText(email);
        }

        if (address != null) {
            etAddress.setText(address);
        }

        if (homePhone != null) {
            etHomePhone.setText(homePhone);
        }

        if (workPhone != null) {
            etWorkPhone.setText(workPhone);
        }

        if (mobilePhone != null) {
            etMobilePhone.setText(mobilePhone);
        }

        if (fax != null) {
            etFax.setText(fax);
        }

        if (country != null) {
            etCountry.setText(country);
        }

        if (state != null) {
            etState.setText(state);
        }

        if (city != null) {
            etCity.setText(city);
        }

        if (zip != null) {
            etZip.setText(zip);
        }
    }
}
