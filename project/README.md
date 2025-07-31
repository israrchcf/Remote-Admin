# Remote Android Monitoring & Administration System

A professional monitoring and administration system consisting of an Android client app and a Laravel web admin panel.

## 🚨 IMPORTANT LEGAL NOTICE
This software is designed for legitimate monitoring purposes such as:
- Parental control with child's knowledge
- Employee monitoring with proper consent and disclosure
- Personal device management

**Usage of this software without proper consent and disclosure may violate privacy laws. Users are responsible for compliance with local regulations.**

## 📁 Project Structure

```
remote-admin-system/
├── android-client/                 # Android Studio project
│   ├── app/
│   │   ├── src/main/java/com/system/service/
│   │   ├── src/main/res/
│   │   ├── src/main/AndroidManifest.xml
│   │   └── google-services.json
│   ├── build.gradle
│   └── README.md
├── web-admin-panel/               # Laravel project
│   ├── app/
│   ├── config/
│   ├── database/
│   ├── resources/
│   ├── routes/
│   ├── composer.json
│   ├── .env.example
│   └── config.json
├── deployment/                    # Deployment scripts and configs
│   ├── hostinger-setup.md
│   ├── firebase-setup.md
│   └── android-build.md
└── docs/                         # Documentation
    ├── api-documentation.md
    ├── user-guide.md
    └── privacy-policy.md
```

## 🚀 Quick Start

### Android Client Setup
1. Open `android-client` in Android Studio
2. Place `google-services.json` in `app/` directory
3. Build and install APK
4. Grant all permissions on first launch

### Web Admin Panel Setup
1. Upload `web-admin-panel` to Hostinger
2. Configure `.env` with database credentials
3. Run `php artisan migrate`
4. Access admin panel at your domain

### Firebase Configuration
- Project ID: `remoteadmin-a1089`
- Package Name: `com.system.service`
- Enable Authentication, Realtime Database, Firestore, Storage, FCM

## 📊 Features

### Android Client
- ✅ Transparent monitoring interface
- ✅ SMS & MMS tracking
- ✅ Call logs and recording
- ✅ GPS location tracking
- ✅ Photo capture and management
- ✅ App usage monitoring
- ✅ Website history tracking
- ✅ Remote commands via FCM
- ✅ Real-time data sync

### Web Admin Panel
- ✅ Material Design dashboard
- ✅ Real-time device monitoring
- ✅ Interactive maps for location tracking
- ✅ Report generation (PDF/Excel/CSV)
- ✅ Remote command console
- ✅ User management system
- ✅ Analytics and insights

## 🔧 Technical Stack

- **Android**: Java, Firebase SDK, WebView
- **Backend**: Laravel 10, PHP 8.1+
- **Database**: MySQL, Firebase Realtime DB, Firestore
- **Frontend**: Material UI, Chart.js, Google Maps
- **Hosting**: Hostinger shared hosting
- **Real-time**: Firebase Cloud Messaging

## 📱 Installation

See individual README files in each project directory for detailed setup instructions.

## 🔒 Security & Privacy

This system implements:
- End-to-end encryption for sensitive data
- Secure authentication via Firebase Auth
- Permission-based access control
- Audit logging for all operations
- GDPR-compliant data handling

## 📞 Support

For technical support or questions, please refer to the documentation in the `docs/` directory.