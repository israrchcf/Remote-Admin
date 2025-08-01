# Remote Admin Panel - React Frontend

A comprehensive React-based admin dashboard for monitoring Android devices in real-time through Firebase integration.

## 🚨 IMPORTANT LEGAL NOTICE
This application is designed for legitimate monitoring purposes such as:
- Parental control with child's knowledge and consent
- Employee monitoring with proper disclosure and legal agreements
- Personal device management and security

**Usage without proper consent and disclosure may violate privacy laws. Users are responsible for compliance with local regulations.**

## 🚀 Features

### Authentication & Security
- **Firebase Authentication**: Email/password login for admin-only access
- **Protected Routes**: Automatic redirect to login for unauthenticated users
- **Secure Session Management**: Firebase-handled authentication state

### Real-time Monitoring Dashboard
- **Device Management**: View all connected devices with online/offline status
- **Location Tracking**: Real-time GPS location monitoring with accuracy indicators
- **Activity Feed**: Live SMS and call log monitoring with contact resolution
- **Statistics Overview**: Device counts, daily activity summaries
- **Interactive Map**: Device location visualization (placeholder for Google Maps)

### Remote Commands
- **Location Requests**: Get current GPS coordinates
- **Media Capture**: Remote photo and audio recording
- **Data Synchronization**: Force SMS/call log sync
- **Service Management**: Restart monitoring services
- **Confirmation System**: Safety prompts for sensitive commands

## 🏗️ Architecture

### Technology Stack
- **React 18.3.1**: Modern React with hooks and functional components
- **TypeScript**: Full type safety and IntelliSense support
- **Tailwind CSS**: Utility-first styling with custom components
- **Firebase**: Authentication, Realtime Database, Firestore integration
- **React Query**: Server state management and caching
- **React Router**: Client-side routing and navigation
- **Lucide React**: Beautiful, consistent icons

### Project Structure
```
src/
├── components/          # Reusable UI components
│   ├── auth/           # Authentication components
│   ├── dashboard/      # Dashboard-specific components
│   └── ui/            # Base UI components (Button, Input, Card)
├── contexts/          # React contexts (Auth)
├── hooks/            # Custom hooks for data fetching
├── lib/              # Utilities and Firebase configuration
├── App.tsx           # Main application component
├── main.tsx          # Application entry point
└── index.css         # Global styles and Tailwind imports
```

### Component Architecture
- **AuthProvider**: Manages authentication state and Firebase auth
- **Dashboard**: Main admin interface with tabbed navigation
- **DeviceList**: Real-time device monitoring with status indicators
- **ActivityFeed**: Live SMS/call activity with contact resolution
- **CommandCenter**: Remote device control with confirmation dialogs
- **LocationMap**: GPS tracking visualization and device positioning

## 🔧 Setup Instructions

### 1. Prerequisites
- Node.js 18+ and npm/yarn
- Firebase project with enabled services
- Admin credentials for dashboard access

### 2. Firebase Configuration

#### Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project: `remoteadmin-yourname`
3. Enable the following services:
   - **Authentication**: Email/Password provider
   - **Realtime Database**: For live device data
   - **Firestore**: For activity logs and analytics
   - **Cloud Messaging**: For remote commands
   - **Storage**: For captured media files

#### Get Configuration Keys
1. Go to Project Settings → General
2. Scroll to "Your apps" section
3. Click "Add app" → Web app
4. Register app and copy configuration values
5. Note down: `apiKey`, `projectId`, `appId`, `messagingSenderId`

### 3. Environment Setup
```bash
# Clone and navigate to React app
cd Remote-Admin

# Install dependencies
npm install

# Create environment file
cp .env.example .env

# Edit .env with your Firebase config
VITE_FIREBASE_API_KEY=your_api_key_here
VITE_FIREBASE_PROJECT_ID=your_project_id_here
VITE_FIREBASE_APP_ID=your_app_id_here
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id_here
```

### 4. Firebase Security Rules

#### Realtime Database Rules
```json
{
  "rules": {
    "devices": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "locations": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

#### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 5. Admin User Creation
```javascript
// Use Firebase Auth Console or create programmatically
const adminEmail = "admin@yourdomain.com";
const adminPassword = "secure_password_here";

// Create user in Firebase Auth Console
// Or use Firebase Admin SDK
```

## 🚀 Development

### Start Development Server
```bash
npm run dev
```
Access dashboard at: `http://localhost:5173`

### Build for Production
```bash
npm run build
npm run preview
```

### Lint and Format
```bash
npm run lint
```

## 📱 Integration with Android Client

### Device Registration
Android devices automatically register when monitoring service starts:
```typescript
// Device data structure
interface Device {
  deviceId: string;
  userId: string;
  status: 'online' | 'offline' | 'away';
  lastSeen: number;
  appVersion: string;
  deviceModel?: string;
  androidVersion?: string;
  fcmToken?: string;
}
```

### Real-time Data Flow
1. **Device Data**: Android → Firebase Realtime Database → React Dashboard
2. **Activity Logs**: Android → Firestore → Activity Feed
3. **Commands**: Dashboard → Firebase → FCM → Android Device
4. **Location**: Android GPS → Firebase → Location Map

### Data Security
- All communication encrypted via HTTPS/WSS
- Firebase security rules restrict admin-only access
- Authentication required for all data operations
- No sensitive data stored in client-side code

## 🔒 Security Features

### Authentication
- Email/password authentication via Firebase Auth
- Session management with automatic logout
- Protected routes with auth state verification
- Secure token storage and refresh

### Data Protection
- Admin-only Firebase security rules
- Encrypted real-time communication
- No storage of credentials in browser
- Audit logging for all admin actions

### Privacy Compliance
- Clear consent requirements in documentation
- Admin authentication required for access
- Device data access controls
- Activity logging for accountability

## 📊 Dashboard Features

### Overview Tab
- **Device Statistics**: Total devices, online count
- **Activity Summary**: Daily SMS/call counts
- **Device List**: Real-time status monitoring
- **Command Center**: Remote device control
- **Recent Activity**: Latest SMS/calls across all devices

### Devices Tab
- **Detailed Device View**: Extended device information
- **Status Monitoring**: Online/offline/away indicators
- **Device Selection**: Click to select for commands
- **Quick Actions**: Immediate command access

### Activity Tab  
- **Comprehensive Feed**: All SMS and calls
- **Contact Resolution**: Display contact names
- **Time Filtering**: Activity by time range
- **Device Filtering**: Activity for specific devices

### Map Tab
- **Location Visualization**: GPS coordinates display
- **Device Positioning**: Real-time location updates
- **Accuracy Indicators**: GPS precision information
- **Location History**: Track device movement

## 🔧 Customization

### Styling
```css
/* Custom theme colors in index.css */
:root {
  --primary-blue: #2563eb;
  --success-green: #16a34a;
  --warning-yellow: #ca8a04;
  --danger-red: #dc2626;
}
```

### Monitoring Intervals
```typescript
// Configure refresh rates in hooks
const DEVICE_REFRESH_INTERVAL = 5000; // 5 seconds
const ACTIVITY_REFRESH_INTERVAL = 10000; // 10 seconds
const LOCATION_REFRESH_INTERVAL = 30000; // 30 seconds
```

### Command Configuration
```typescript
// Add custom commands in CommandCenter.tsx
const customCommands = [
  {
    id: 'custom_action',
    name: 'Custom Action',
    description: 'Execute custom device action',
    icon: <YourIcon />,
    variant: 'primary',
  }
];
```

## 🐛 Troubleshooting

### Common Issues

1. **Firebase Connection Errors**
   - Check environment variables are set correctly
   - Verify Firebase project configuration
   - Ensure Firebase services are enabled

2. **Authentication Problems**
   - Check Firebase Auth email/password provider is enabled
   - Verify admin user exists in Firebase Auth console
   - Clear browser cache and try again

3. **Real-time Data Not Loading**
   - Check Firebase Realtime Database rules
   - Verify Android app is writing data to correct paths
   - Check network connectivity and browser console

4. **Commands Not Working**
   - Verify FCM is enabled in Firebase project
   - Check Android app FCM token registration
   - Ensure device is online and connected

### Debug Tools
```bash
# Check environment variables
echo $VITE_FIREBASE_PROJECT_ID

# View Firebase Auth state
# Open browser dev tools → Application → Firebase

# Monitor real-time data
# Firebase Console → Realtime Database → View data
```

## 📄 License

MIT License - See LICENSE file for details.

## ⚖️ Legal Disclaimer

This software is provided for legitimate monitoring purposes only. Users must:
- Obtain proper consent before monitoring
- Comply with local privacy laws  
- Provide clear disclosure to monitored parties
- Use responsibly and ethically

The developers are not responsible for misuse of this software.