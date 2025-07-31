package com.system.service.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.HashMap;
import java.util.Map;

/**
 * Location Manager - Handles GPS location tracking
 * Provides continuous location monitoring and data storage
 */
public class LocationManager {
    
    private static final String TAG = "LocationManager";
    private static final long LOCATION_UPDATE_INTERVAL = 60000; // 1 minute
    private static final long FASTEST_UPDATE_INTERVAL = 30000; // 30 seconds
    
    private Context context;
    private DataManager dataManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Handler handler;
    
    private boolean isTracking = false;
    private Location lastKnownLocation;
    
    public LocationManager(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.handler = new Handler(Looper.getMainLooper());
        
        initializeLocationRequest();
        initializeLocationCallback();
    }
    
    /**
     * Initialize location request parameters
     */
    private void initializeLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
                .build();
    }
    
    /**
     * Initialize location callback for receiving updates
     */
    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        handleLocationUpdate(location);
                    }
                }
            }
        };
    }
    
    /**
     * Start location tracking
     */
    public void startLocationTracking() {
        if (isTracking) {
            Log.d(TAG, "Location tracking already started");
            return;
        }
        
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted");
            return;
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            isTracking = true;
            Log.d(TAG, "Location tracking started");
            
            // Get last known location immediately
            getLastKnownLocation();
            
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception starting location tracking", e);
        } catch (Exception e) {
            Log.e(TAG, "Error starting location tracking", e);
        }
    }
    
    /**
     * Stop location tracking
     */
    public void stopLocationTracking() {
        if (!isTracking) {
            return;
        }
        
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
            Log.d(TAG, "Location tracking stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location tracking", e);
        }
    }
    
    /**
     * Get last known location
     */
    private void getLastKnownLocation() {
        if (!hasLocationPermission()) {
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        handleLocationUpdate(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get last known location", e);
                });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting last known location", e);
        }
    }
    
    /**
     * Handle location update and store data
     */
    private void handleLocationUpdate(Location location) {
        lastKnownLocation = location;
        
        // Create location data map
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("accuracy", location.getAccuracy());
        locationData.put("altitude", location.getAltitude());
        locationData.put("speed", location.getSpeed());
        locationData.put("bearing", location.getBearing());
        locationData.put("provider", location.getProvider());
        locationData.put("timestamp", location.getTime());
        
        // Add additional metadata
        locationData.put("batteryLevel", getBatteryLevel());
        locationData.put("networkType", getNetworkType());
        
        // Store location data
        dataManager.storeLocationData(locationData);
        
        Log.d(TAG, String.format("Location updated: %.6f, %.6f (accuracy: %.1fm)", 
            location.getLatitude(), location.getLongitude(), location.getAccuracy()));
    }
    
    /**
     * Check if location permission is granted
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get current battery level
     */
    private int getBatteryLevel() {
        try {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = context.registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            return (int) ((level / (float) scale) * 100);
        } catch (Exception e) {
            Log.e(TAG, "Error getting battery level", e);
            return -1;
        }
    }
    
    /**
     * Get current network type
     */
    private String getNetworkType() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            
            if (activeNetwork != null && activeNetwork.isConnected()) {
                switch (activeNetwork.getType()) {
                    case android.net.ConnectivityManager.TYPE_WIFI:
                        return "WiFi";
                    case android.net.ConnectivityManager.TYPE_MOBILE:
                        return "Mobile";
                    default:
                        return "Other";
                }
            }
            return "None";
        } catch (Exception e) {
            Log.e(TAG, "Error getting network type", e);
            return "Unknown";
        }
    }
    
    /**
     * Get last known location
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
    
    /**
     * Check if location tracking is active
     */
    public boolean isTracking() {
        return isTracking;
    }
    
    /**
     * Force location update
     */
    public void forceLocationUpdate() {
        if (isTracking) {
            getLastKnownLocation();
        }
    }
}