package ru.assisttech.assistsdk;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import ru.assisttech.sdk.AssistSDK;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvVersion = (TextView)findViewById(R.id.tvAppVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(String.format(getString(R.string.app_version), pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText(String.format(getString(R.string.app_version), "unknown"));
            e.printStackTrace();
        }
        TextView tvSDKVersion = (TextView)findViewById(R.id.tvSDKVersion);
        tvSDKVersion.setText(String.format(getString(R.string.sdk_version), AssistSDK.getSdkVersion()));
    }
}
