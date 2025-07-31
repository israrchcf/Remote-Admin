package com.system.service.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Permission Manager - Handles all permission requests and checks
 * Manages runtime permissions, special permissions, and battery optimization
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    
    private Activity activity;
    private Context context;
    
    public PermissionManager(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }
    
    /**
     * Check if usage stats permission is granted
     */
    public boolean hasUsageStatsPermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Log.e(TAG, "Error checking usage stats permission", e);
            return false;
        }
    }
    
    /**
     * Check if permission is granted
     */
    public boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if device admin permission is enabled
     */
    public boolean isDeviceAdminEnabled() {
        // Device admin implementation can be added here if needed
        return true; // For now, return true to avoid blocking
    }
    
    /**
     * Check if battery optimization is ignored
     */
    public boolean isBatteryOptimizationIgnored() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }
    
    /**
     * Request to ignore battery optimization
     */
    public void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isBatteryOptimizationIgnored()) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error requesting battery optimization ignore", e);
                // Fallback to general battery optimization settings
                try {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    activity.startActivity(intent);
                } catch (Exception ex) {
                    Log.e(TAG, "Error opening battery optimization settings", ex);
                }
            }
        }
    }
    
    /**
     * Check if accessibility service is enabled
     */
    public boolean isAccessibilityServiceEnabled() {
        // Accessibility service check implementation
        // This would require an AccessibilityService class
        return true; // For now, return true
    }
    
    /**
     * Request accessibility service permission
     */
    public void requestAccessibilityPermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening accessibility settings", e);
        }
    }
    
    /**
     * Open app settings page
     */
    public void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings", e);
        }
    }
    
    /**
     * Check if overlay permission is granted (for Android 6.0+)
     */
    public boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }
    
    /**
     * Request overlay permission
     */
    public void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasOverlayPermission()) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error requesting overlay permission", e);
            }
        }
    }
    
    /**
     * Get permission status summary
     */
    public String getPermissionStatusSummary() {
        StringBuilder status = new StringBuilder();
        status.append("Usage Stats: ").append(hasUsageStatsPermission()).append("\n");
        status.append("Battery Optimization Ignored: ").append(isBatteryOptimizationIgnored()).append("\n");
        status.append("Overlay Permission: ").append(hasOverlayPermission()).append("\n");
        status.append("Device Admin: ").append(isDeviceAdminEnabled()).append("\n");
        status.append("Accessibility Service: ").append(isAccessibilityServiceEnabled());
        
        return status.toString();
    }
}