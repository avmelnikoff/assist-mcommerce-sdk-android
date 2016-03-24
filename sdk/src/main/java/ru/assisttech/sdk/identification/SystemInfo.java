package ru.assisttech.sdk.identification;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SystemInfo {

	private static SystemInfo instance;
	private DeviceLocation devLocation;
	private String fingerprint = "";
	private String aId = "";
	private String devSerial = "";
	private String device = "";
	private String macAddress = "";
	private String language = "";
	
	private static final String TAG = "SystemInfo";
	
	public static SystemInfo getInstance(Context context) {
		if(instance == null)
			instance = new SystemInfo(context);
		return instance;
	}	
	
	public void startLocation() {
		devLocation.startPositioning();
	}
	
	public void stopLocation() {
		devLocation.stopPositioning();
	}
	
	public String fingerprint() {
		return fingerprint;
	}	
	
	public String androidID() {
		return aId;
	}
	
	public String serialNo() {
		return devSerial;
	}
	
	public String device() {
		return device;
	}
	
	public String longitude() {
		return devLocation.getLongitude();
	}
	
	public String lattitude() {
		return devLocation.getLatitude();
	}
		
	public String macAddress() {
		return macAddress;
	}
			
	public String language() {
		return language;
	}		
		
	private SystemInfo(Context context) {
		
		devLocation = new DeviceLocation(context);
						
		device = Build.MODEL;
		devSerial = Build.SERIAL;
		if (devSerial == null)
			devSerial = "";

        aId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        Configuration cfg = context.getResources().getConfiguration();
        language = cfg.locale.getDisplayLanguage();
		
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
		    WifiInfo info = wifiManager.getConnectionInfo();
		    macAddress = info.getMacAddress();
		}

		TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tManager.getDeviceId();	
		String subscriberId = tManager.getSubscriberId();
				
		if (deviceId == null) {
			deviceId = "";
		}
		
		if (subscriberId == null) {
			subscriberId = "";
		}
		/*
		Log.d(TAG, "Device ID: 	   " + deviceId);
		Log.d(TAG, "Subscriber ID: " + subscriberId);
		Log.d(TAG, "Device: 	   " + device);
		Log.d(TAG, "Serial: 	   " + devSerial);
		Log.d(TAG, "Android ID:    " + aId);
		Log.d(TAG, "MAC:           " + macAddress);
		Log.d(TAG, "Language:      " + language);
		*/
		Long devIdLong = 0L;
		Long subscrIdLong = 0L;
		Long serialLong = 0L;
		
		try {
			devIdLong = Long.decode(deviceId);
		} catch (NumberFormatException e) {
			devIdLong = 1L;
		}
				
		try {
			subscrIdLong = Long.decode(subscriberId);
		} catch (NumberFormatException e) {
			subscrIdLong = 1L;
		}		
		
		try {
			serialLong = Long.decode(devSerial);
		} catch (NumberFormatException e) {
			serialLong = 1L;
		}
		
		if (!deviceId.isEmpty() && !deviceId.equalsIgnoreCase("unknown") && (devIdLong != 0L)) {
			fingerprint = deviceId;
			fingerprint = "IMEI" + fingerprint;
		} else if (!subscriberId.isEmpty() && !subscriberId.equalsIgnoreCase("unknown") && (subscrIdLong != 0L)) {
			fingerprint = subscriberId;
			fingerprint = "IMSI" + fingerprint;
		} else if (!devSerial.isEmpty() && !devSerial.equalsIgnoreCase("unknown") && (serialLong != 0L)) {
			fingerprint = devSerial;
			fingerprint = "SERIAL" + fingerprint;
		} else {
			fingerprint = aId;
			fingerprint = "AID" + fingerprint;
		}				
		//Log.d(TAG, "Fingerprint: " + fingerprint);
	}
}
