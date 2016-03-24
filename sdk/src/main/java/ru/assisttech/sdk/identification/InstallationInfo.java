package ru.assisttech.sdk.identification;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InstallationInfo {
	
	private static final String INSTALLATION = ".mobilepayinstallation";
	private static final String APP_REG_ID = "ApplicationRegId";

    private static InstallationInfo instance;
    private Context context;
    private SharedPreferences settings;
    private String appName;
    private String versionName;

    public static InstallationInfo getInstance(Context context){
        if(instance == null)
            instance = new InstallationInfo(context);
        return instance;
    }
    
    private InstallationInfo(Context context) {
    	this.context = context;
    	getNames(context);
        // Restore preferences
        String name = context.getApplicationInfo().packageName + INSTALLATION;
        settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public String getMerchantRegId(String merchantID) {
        return settings.getString(merchantID, null);
    }

    public String getAppRegId() {
        return settings.getString(APP_REG_ID, null);
    }

    public void setAppRegID(String registrationID) {
        setRegistrationId(APP_REG_ID, registrationID);
    }

    public void setMerchantRegID(String merchantID, String registrationID) {
        setRegistrationId(merchantID, registrationID);
    }

    private void setRegistrationId(String merchantID, String registrationID) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(merchantID, registrationID);
        // Commit the edits!
        editor.commit();
    }

    public String appName(){
        return appName;
    }

    public String versionName(){
        return versionName;
    }

    public String installedApplications() {

        StringBuilder apps = new StringBuilder();
        apps.append("<device os=\"android\">");

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (int i = 0; i < packages.size(); i++) {
            try {
                // App name
                String name = packages.get(i).loadLabel(pm).toString();
                PackageInfo pi = pm.getPackageInfo(packages.get(i).packageName, 0);
                // App version
                String version = pi.versionName;
                // App date
                long mills = pi.lastUpdateTime;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US);
                String date = sdf.format(new Date(mills));
                // Form string
                apps.append("<app name=\"").
                     append(name).
                     append("\" ver=\"").
                     append(version).
                     append("\" date=\"").
                     append(date).
                     append("\">");
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        apps.append("</device>");
        return apps.toString();
    }
    
    private void getNames(Context context) {
    	
    	Resources res = context.getResources();
        appName = res.getText(res.getIdentifier("app_name", "string", context.getPackageName())).toString();
    	    	    	
    	versionName = "";
    	PackageManager manager = context.getPackageManager();	    	
		try {
			PackageInfo pacInfo = manager.getPackageInfo(context.getPackageName(), 0);
			versionName = pacInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}	
    }
}
