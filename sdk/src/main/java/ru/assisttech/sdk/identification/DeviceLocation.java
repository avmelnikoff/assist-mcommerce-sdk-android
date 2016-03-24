package ru.assisttech.sdk.identification;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class DeviceLocation {
	
	private static final String TAG = "DeviceLocation";
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private boolean _networkProviderEnabled;
	private LocationManager _locationManager;
	private LocationListener _locationListener;
	private Location _location;
	
	public DeviceLocation(Context context) {

		Location lastKnownNetworkLocation = null;
		
		// Acquire a reference to the system Location Manager
		_locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		/*
		// List all providers
		Log.d(TAG, "Providers:");
		List<String> providers = _locationManager.getAllProviders();
		for (String provider: providers) {
			Log.d(TAG, " - " + provider);
		}		
		// List enabled providers
		Log.d(TAG, "Enabled providers:");
		List<String> enabledProviders = _locationManager.getProviders(true);
		for (String provider: enabledProviders) {
			Log.d(TAG, " - " + provider);
		}
		*/
		if (_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			_networkProviderEnabled = true;			
			lastKnownNetworkLocation = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);		
			if (lastKnownNetworkLocation != null) {
				Log.d(TAG, "Last known location Network - lat: " + 
							String.valueOf(lastKnownNetworkLocation.getLatitude()) + "; lon: " + 
							String.valueOf(lastKnownNetworkLocation.getLongitude()) + ";");
			} else {
				Log.d(TAG, "location unknown");
			}
		}
					
		if (lastKnownNetworkLocation != null) {
			_location = lastKnownNetworkLocation;
		}

		// Define a listener that responds to location updates
		_locationListener = new LocListener();
		startPositioning();
	}
	
	public void startPositioning() {
		// Register the listener with the Location Manager to receive location updates
		if (_networkProviderEnabled) {
			_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, _locationListener);
		}
	}
	
	public void stopPositioning() {
		_locationManager.removeUpdates(_locationListener);
	}
	
	public String getLatitude() {
		if (_location != null)
			return String.valueOf(_location.getLatitude());
		else
			return "";
	}
	
	public String getLongitude() {
		if (_location != null)
			return String.valueOf(_location.getLongitude());
		else
			return "";
	}
	
	private class LocListener implements LocationListener {
		
		@Override
	    public void onLocationChanged(Location location) {
			if(isBetterLocation(location, _location))
				_location = location;							
	    }
	    
		@Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    
	    @Override
	    public void onProviderEnabled(String provider) {
	    	if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
	    		_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, _locationListener);
	    	}
	    }
	    
	    @Override
	    public void onProviderDisabled(String provider) {}
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		
		if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
