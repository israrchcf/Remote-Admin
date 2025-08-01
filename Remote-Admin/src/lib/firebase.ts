import { initializeApp } from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';
import { getDatabase, connectDatabaseEmulator } from 'firebase/database';
import { getFirestore, connectFirestoreEmulator } from 'firebase/firestore';
import { getStorage, connectStorageEmulator } from 'firebase/storage';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

// Firebase configuration
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: `${import.meta.env.VITE_FIREBASE_PROJECT_ID}.firebaseapp.com`,
  databaseURL: `https://${import.meta.env.VITE_FIREBASE_PROJECT_ID}-default-rtdb.firebaseio.com`,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: `${import.meta.env.VITE_FIREBASE_PROJECT_ID}.appspot.com`,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Firebase services
export const auth = getAuth(app);
export const db = getDatabase(app);
export const firestore = getFirestore(app);
export const storage = getStorage(app);

// Initialize messaging if supported
let messaging: ReturnType<typeof getMessaging> | null = null;
try {
  if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
    messaging = getMessaging(app);
  }
} catch (error) {
  console.warn('Firebase messaging not available:', error);
}

export { messaging };

// Development emulator connections
if (import.meta.env.DEV) {
  // Only connect to emulators in development mode
  try {
    // Uncomment these lines if you want to use Firebase emulators
    // connectAuthEmulator(auth, 'http://localhost:9099');
    // connectDatabaseEmulator(db, 'localhost', 9000);
    // connectFirestoreEmulator(firestore, 'localhost', 8080);
    // connectStorageEmulator(storage, 'localhost', 9199);
  } catch (error) {
    console.warn('Firebase emulator connection failed:', error);
  }
}

// FCM token management
export const getFCMToken = async (): Promise<string | null> => {
  if (!messaging) return null;
  
  try {
    const token = await getToken(messaging, {
      vapidKey: import.meta.env.VITE_FIREBASE_VAPID_KEY,
    });
    return token;
  } catch (error) {
    console.error('Error getting FCM token:', error);
    return null;
  }
};

// FCM message listener
export const onFCMMessage = (callback: (payload: any) => void) => {
  if (!messaging) return () => {};
  
  return onMessage(messaging, callback);
};

export default app;