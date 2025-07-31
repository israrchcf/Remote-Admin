package com.system.service.manager;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * App Usage Manager - Handles app usage monitoring and statistics
 * Tracks app usage time, foreground events, and usage patterns
 */
public class AppUsageManager {
    
    private static final String TAG = "AppUsageManager";
    private static final long USAGE_CHECK_INTERVAL = 300000; // 5 minutes
    
    private Context context;
    private DataManager dataManager;
    private UsageStatsManager usageStatsManager;
    private PackageManager packageManager;
    private Handler handler;
    private ExecutorService executorService;
    
    private boolean isMonitoring = false;
    private long lastUsageTimestamp = 0;
    
    public AppUsageManager(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        this.packageManager = context.getPackageManager();
        this.handler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Initialize last usage timestamp
        initializeLastUsageTimestamp();
    }
    
    /**
     * Initialize last usage timestamp to current time
     */
    private void initializeLastUsageTimestamp() {
        lastUsageTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Start app usage monitoring
     */
    public void startAppUsageMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "App usage monitoring already started");
            return;
        }
        
        if (!hasUsageStatsPermission()) {
            Log.e(TAG, "Usage stats permission not granted");
            return;
        }
        
        isMonitoring = true;
        Log.d(TAG, "App usage monitoring started");
        
        // Start periodic usage checking
        scheduleUsageCheck();
    }
    
    /**
     * Stop app usage monitoring
     */
    public void stopAppUsageMonitoring() {
        if (!isMonitoring) {
            return;
        }
        
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "App usage monitoring stopped");
    }
    
    /**
     * Schedule periodic usage checking
     */
    private void scheduleUsageCheck() {
        if (!isMonitoring) {
            return;
        }
        
        handler.postDelayed(() -> {
            executorService.execute(this::checkAppUsage);
            scheduleUsageCheck(); // Schedule next check
        }, USAGE_CHECK_INTERVAL);
    }
    
    /**
     * Check app usage statistics
     */
    private void checkAppUsage() {
        if (!hasUsageStatsPermission()) {
            return;
        }
        
        try {
            // Get usage stats for the last hour
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            long startTime = calendar.getTimeInMillis();
            
            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_HOURLY, startTime, endTime);
            
            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                for (UsageStats usageStats : usageStatsList) {
                    if (usageStats.getTotalTimeInForeground() > 0) {
                        Map<String, Object> appUsageData = extractAppUsageData(usageStats);
                        if (appUsageData != null) {
                            dataManager.storeAppUsageData(appUsageData);
                            
                            Log.d(TAG, String.format("App usage: %s used for %d ms", 
                                usageStats.getPackageName(), 
                                usageStats.getTotalTimeInForeground()));
                        }
                    }
                }
            }
            
            // Also get recent events
            checkUsageEvents();
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking app usage", e);
        }
    }
    
    /**
     * Check usage events for detailed activity
     */
    private void checkUsageEvents() {
        try {
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.MINUTE, -30); // Last 30 minutes
            long startTime = calendar.getTimeInMillis();
            
            android.app.usage.UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
            android.app.usage.UsageEvents.Event event = new android.app.usage.UsageEvents.Event();
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                
                if (event.getTimeStamp() > lastUsageTimestamp) {
                    Map<String, Object> eventData = extractUsageEventData(event);
                    if (eventData != null) {
                        dataManager.storeAppUsageData(eventData);
                    }
                }
            }
            
            lastUsageTimestamp = endTime;
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking usage events", e);
        }
    }
    
    /**
     * Extract app usage data from UsageStats
     */
    private Map<String, Object> extractAppUsageData(UsageStats usageStats) {
        try {
            Map<String, Object> appUsageData = new HashMap<>();
            
            String packageName = usageStats.getPackageName();
            
            // Basic usage information
            appUsageData.put("package_name", packageName);
            appUsageData.put("total_time_foreground", usageStats.getTotalTimeInForeground());
            appUsageData.put("first_time_stamp", usageStats.getFirstTimeStamp());
            appUsageData.put("last_time_stamp", usageStats.getLastTimeStamp());
            appUsageData.put("last_time_used", usageStats.getLastTimeUsed());
            
            // App information
            String appName = getAppName(packageName);
            appUsageData.put("app_name", appName);
            appUsageData.put("app_category", getAppCategory(packageName));
            appUsageData.put("is_system_app", isSystemApp(packageName));
            
            // Usage metrics
            appUsageData.put("usage_type", "statistics");
            appUsageData.put("timestamp", System.currentTimeMillis());
            appUsageData.put("usage_duration_ms", usageStats.getTotalTimeInForeground());
            appUsageData.put("usage_duration_formatted", formatDuration(usageStats.getTotalTimeInForeground()));
            
            return appUsageData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting app usage data", e);
            return null;
        }
    }
    
    /**
     * Extract usage event data
     */
    private Map<String, Object> extractUsageEventData(android.app.usage.UsageEvents.Event event) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            
            String packageName = event.getPackageName();
            
            // Basic event information
            eventData.put("package_name", packageName);
            eventData.put("class_name", event.getClassName());
            eventData.put("event_type", getEventTypeString(event.getEventType()));
            eventData.put("event_type_code", event.getEventType());
            eventData.put("timestamp", event.getTimeStamp());
            
            // App information
            String appName = getAppName(packageName);
            eventData.put("app_name", appName);
            eventData.put("app_category", getAppCategory(packageName));
            eventData.put("is_system_app", isSystemApp(packageName));
            
            // Event metadata
            eventData.put("usage_type", "event");
            eventData.put("event_description", getEventDescription(event.getEventType()));
            
            return eventData;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting usage event data", e);
            return null;
        }
    }
    
    /**
     * Get app name from package name
     */
    private String getAppName(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting app name for " + packageName, e);
            return packageName;
        }
    }
    
    /**
     * Get app category
     */
    private String getAppCategory(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int category = appInfo.category;
                switch (category) {
                    case ApplicationInfo.CATEGORY_GAME:
                        return "Game";
                    case ApplicationInfo.CATEGORY_AUDIO:
                        return "Audio";
                    case ApplicationInfo.CATEGORY_VIDEO:
                        return "Video";
                    case ApplicationInfo.CATEGORY_IMAGE:
                        return "Image";
                    case ApplicationInfo.CATEGORY_SOCIAL:
                        return "Social";
                    case ApplicationInfo.CATEGORY_NEWS:
                        return "News";
                    case ApplicationInfo.CATEGORY_MAPS:
                        return "Maps";
                    case ApplicationInfo.CATEGORY_PRODUCTIVITY:
                        return "Productivity";
                    default:
                        return "Other";
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Check if app is system app
     */
    private boolean isSystemApp(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get event type as string
     */
    private String getEventTypeString(int eventType) {
        switch (eventType) {
            case android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND:
                return "foreground";
            case android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND:
                return "background";
            case android.app.usage.UsageEvents.Event.CONFIGURATION_CHANGE:
                return "configuration_change";
            case android.app.usage.UsageEvents.Event.USER_INTERACTION:
                return "user_interaction";
            case android.app.usage.UsageEvents.Event.SHORTCUT_INVOCATION:
                return "shortcut_invocation";
            default:
                return "unknown";
        }
    }
    
    /**
     * Get event description
     */
    private String getEventDescription(int eventType) {
        switch (eventType) {
            case android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND:
                return "App moved to foreground";
            case android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND:
                return "App moved to background";
            case android.app.usage.UsageEvents.Event.CONFIGURATION_CHANGE:
                return "Configuration changed";
            case android.app.usage.UsageEvents.Event.USER_INTERACTION:
                return "User interaction";
            case android.app.usage.UsageEvents.Event.SHORTCUT_INVOCATION:
                return "Shortcut invoked";
            default:
                return "Unknown event";
        }
    }
    
    /**
     * Format duration in human readable format
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    /**
     * Check if usage stats permission is granted
     */
    private boolean hasUsageStatsPermission() {
        try {
            long time = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time);
            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get app usage statistics summary
     */
    public Map<String, Object> getUsageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (hasUsageStatsPermission()) {
                Calendar calendar = Calendar.getInstance();
                long endTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DAY_OF_MONTH, -1); // Last 24 hours
                long startTime = calendar.getTimeInMillis();
                
                List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
                
                if (usageStatsList != null) {
                    stats.put("apps_used_today", usageStatsList.size());
                    
                    long totalUsageTime = 0;
                    for (UsageStats usageStats : usageStatsList) {
                        totalUsageTime += usageStats.getTotalTimeInForeground();
                    }
                    stats.put("total_usage_time_ms", totalUsageTime);
                    stats.put("total_usage_time_formatted", formatDuration(totalUsageTime));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting usage statistics", e);
        }
        
        stats.put("monitoring_active", isMonitoring);
        stats.put("has_permission", hasUsageStatsPermission());
        stats.put("last_check_timestamp", lastUsageTimestamp);
        
        return stats;
    }
    
    /**
     * Force usage sync
     */
    public void forceUsageSync() {
        if (isMonitoring && hasUsageStatsPermission()) {
            executorService.execute(this::checkAppUsage);
        }
    }
}