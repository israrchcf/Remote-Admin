package com.system.service;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class for Browser app
 * Handles global initialization and configuration
 */
public class BrowserApplication extends Application {
    
    public static final String NOTIFICATION_CHANNEL_ID = "browser_service_channel";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Create notification channel for foreground service
        createNotificationChannel();
    }
    
    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Browser Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Browser background service");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}