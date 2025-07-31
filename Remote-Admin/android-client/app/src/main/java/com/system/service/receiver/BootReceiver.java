package com.system.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.system.service.service.MonitoringService;

/**
 * Boot Receiver - Handles device boot events
 * Automatically starts monitoring service after device reboot
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            
            Log.d(TAG, "Device boot completed, starting monitoring service");
            
            try {
                // Start monitoring service
                Intent serviceIntent = new Intent(context, MonitoringService.class);
                serviceIntent.putExtra("started_by", "boot_receiver");
                serviceIntent.putExtra("boot_time", System.currentTimeMillis());
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                
                Log.d(TAG, "Monitoring service started successfully after boot");
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to start monitoring service after boot", e);
            }
        }
    }
}