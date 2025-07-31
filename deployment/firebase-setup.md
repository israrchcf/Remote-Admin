# Firebase Setup Guide

## Project Configuration

### 1. Firebase Console Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `remoteadmin-a1089`
3. Verify project configuration

### 2. Authentication Setup

1. **Enable Authentication**
   - Go to Authentication > Sign-in method
   - Enable Email/Password authentication
   - Disable email verification (as specified)

2. **Create Admin User**
   ```javascript
   // In Firebase Console > Authentication > Users
   Email: admin@example.com
   Password: admin1234
   ```

### 3. Realtime Database Configuration

1. **Create Database**
   - Go to Realtime Database
   - Start in test mode initially
   - Choose region: us-central1

2. **Security Rules**
   ```json
   {
     "rules": {
       "devices": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       },
       "sms": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       },
       "calls": {
         "$deviceId": {
           ".read": "auth != null", 
           ".write": "auth != null"
         }
       },
       "locations": {
         "current": {
           "$deviceId": {
             ".read": "auth != null",
             ".write": "auth != null"
           }
         }
       },
       "commands": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       },
       "app_usage": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       },
       "contacts": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       },
       "photos": {
         "$deviceId": {
           ".read": "auth != null",
           ".write": "auth != null"
         }
       }
     }
   }
   ```

### 4. Firestore Configuration

1. **Create Firestore Database**
   - Go to Firestore Database
   - Start in test mode
   - Choose region: us-central1

2. **Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Allow read/write for authenticated users
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

3. **Create Collections**
   - `devices`
   - `sms`
   - `calls`
   - `locations`
   - `app_usage`
   - `contacts`
   - `photos`

### 5. Cloud Storage Configuration

1. **Create Storage Bucket**
   - Go to Storage
   - Use default bucket: `remoteadmin-a1089.firebasestorage.app`

2. **Security Rules**
   ```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /{allPaths=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

3. **Folder Structure**
   ```
   storage/
   ├── photos/
   │   └── {deviceId}/
   ├── audio/
   │   └── {deviceId}/
   ├── documents/
   │   └── {deviceId}/
   └── reports/
       └── {userId}/
   ```

### 6. Cloud Messaging (FCM) Setup

1. **Enable FCM**
   - FCM is enabled by default
   - No additional configuration needed

2. **Server Key**
   - Go to Project Settings > Cloud Messaging
   - Copy Server Key for backend use

### 7. Admin SDK Configuration

1. **Generate Service Account Key**
   - Go to Project Settings > Service Accounts
   - Click "Generate new private key"
   - Download JSON file
   - Rename to `firebase-credentials.json`

2. **Place Credentials**
   ```bash
   # For Laravel project
   cp firebase-credentials.json web-admin-panel/config/
   ```

### 8. Analytics (Optional)

1. **Enable Analytics**
   - Go to Analytics
   - Link to Google Analytics property
   - Configure conversion events

## Testing Firebase Configuration

### 1. Test Authentication
```javascript
// Test in Firebase Console
firebase.auth().signInWithEmailAndPassword("admin@example.com", "admin1234")
  .then((userCredential) => {
    console.log("Authentication successful:", userCredential.user);
  })
  .catch((error) => {
    console.error("Authentication failed:", error);
  });
```

### 2. Test Realtime Database
```javascript
// Test write
firebase.database().ref('test').set({
  message: "Hello Firebase!",
  timestamp: firebase.database.ServerValue.TIMESTAMP
});

// Test read
firebase.database().ref('test').once('value').then((snapshot) => {
  console.log("Data:", snapshot.val());
});
```

### 3. Test Firestore
```javascript
// Test write
db.collection("test").add({
  message: "Hello Firestore!",
  timestamp: firebase.firestore.FieldValue.serverTimestamp()
});

// Test read
db.collection("test").get().then((querySnapshot) => {
  querySnapshot.forEach((doc) => {
    console.log(doc.data());
  });
});
```

### 4. Test Storage
```javascript
// Test upload
const ref = firebase.storage().ref('test/test.txt');
ref.putString('Hello Storage!').then((snapshot) => {
  console.log('Uploaded successfully!');
});
```

## Security Hardening

### 1. Production Security Rules

**Realtime Database:**
```json
{
  "rules": {
    "devices": {
      "$deviceId": {
        ".read": "auth != null && (auth.uid == $deviceId || root.child('admins').child(auth.uid).exists())",
        ".write": "auth != null && (auth.uid == $deviceId || root.child('admins').child(auth.uid).exists())"
      }
    },
    "sms": {
      "$deviceId": {
        ".read": "auth != null && root.child('admins').child(auth.uid).exists()",
        ".write": "auth != null && auth.uid == $deviceId"
      }
    },
    "admins": {
      ".read": false,
      ".write": false
    }
  }
}
```

**Firestore:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Admin access
    match /{document=**} {
      allow read, write: if request.auth != null && 
        exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }
    
    // Device access to own data
    match /devices/{deviceId} {
      allow read, write: if request.auth != null && request.auth.uid == deviceId;
    }
  }
}
```

### 2. App Check (Recommended)
```javascript
// Enable App Check
firebase.appCheck().activate('site-key', true);
```

### 3. Security Monitoring
- Enable audit logs
- Set up security alerts
- Monitor authentication events

## Performance Optimization

### 1. Database Indexing
```javascript
// Create indexes for common queries
// In Firebase Console > Firestore > Indexes
```

### 2. Connection Optimization
```javascript
// Enable offline persistence
firebase.firestore().enablePersistence();

// Optimize Realtime Database
firebase.database().goOnline();
firebase.database().goOffline();
```

### 3. Bandwidth Optimization
```javascript
// Use compression
firebase.database().ref().on('value', snapshot => {
  // Process data
}, {
  compress: true
});
```

## Monitoring & Alerts

### 1. Firebase Analytics
- Set up custom events
- Monitor user engagement
- Track app performance

### 2. Crashlytics
```gradle
// Add to Android app
implementation 'com.google.firebase:firebase-crashlytics'
```

### 3. Performance Monitoring
```gradle
// Add to Android app
implementation 'com.google.firebase:firebase-perf'
```

## Backup Strategy

### 1. Automated Backups
- Enable daily Firestore exports
- Set up Cloud Storage backups
- Configure retention policies

### 2. Manual Backup
```bash
# Export Firestore data
gcloud firestore export gs://backup-bucket/firestore-backup

# Export Realtime Database
firebase database:get / > database-backup.json
```

## Troubleshooting

### Common Issues

1. **Authentication Errors**
   - Verify API keys
   - Check security rules
   - Ensure user exists

2. **Database Permission Denied**
   - Review security rules
   - Check authentication state
   - Verify user permissions

3. **Storage Upload Fails**
   - Check file size limits
   - Verify storage rules
   - Check file type restrictions

4. **FCM Not Working**
   - Verify server key
   - Check app registration
   - Test token generation

### Debug Tools
```javascript
// Enable debug logging
firebase.firestore.setLogLevel('debug');
firebase.database.enableLogging(true);
```

## Quota Management

### 1. Monitor Usage
- Check Firebase Console quotas
- Set up billing alerts
- Monitor API usage

### 2. Optimize Costs
- Use appropriate pricing tier
- Optimize query patterns
- Implement data lifecycle policies

### 3. Scaling Considerations
- Plan for growth
- Implement sharding if needed
- Consider multi-region deployment