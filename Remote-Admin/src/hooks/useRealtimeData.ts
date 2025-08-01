import { useEffect, useState } from 'react';
import { ref, onValue, off, DataSnapshot } from 'firebase/database';
import { collection, onSnapshot, query, orderBy, limit, Unsubscribe } from 'firebase/firestore';
import { db, firestore } from '../lib/firebase';

// Device data interface
export interface Device {
  deviceId: string;
  userId: string;
  status: 'online' | 'offline' | 'away';
  lastSeen: number;
  appVersion: string;
  fcmToken?: string;
  deviceModel?: string;
  androidVersion?: string;
}

// Location data interface
export interface LocationData {
  deviceId: string;
  latitude: number;
  longitude: number;
  accuracy: number;
  timestamp: number;
  altitude?: number;
  speed?: number;
  provider?: string;
}

// SMS data interface
export interface SmsData {
  deviceId: string;
  type: 'received' | 'sent';
  address: string;
  body: string;
  timestamp: number;
  contactName?: string;
  isRead: boolean;
}

// Call data interface
export interface CallData {
  deviceId: string;
  phoneNumber: string;
  type: string;
  duration: number;
  timestamp: number;
  contactName?: string;
  callDirection: 'inbound' | 'outbound';
}

// App usage data interface
export interface AppUsageData {
  deviceId: string;
  packageName: string;
  appName: string;
  totalTimeInForeground: number;
  timestamp: number;
  usageType: 'statistics' | 'event';
}

// Custom hook for real-time devices
export function useRealtimeDevices() {
  const [devices, setDevices] = useState<Record<string, Device>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const devicesRef = ref(db, 'devices');

    const handleValue = (snapshot: DataSnapshot) => {
      try {
        const data = snapshot.val();
        setDevices(data || {});
        setLoading(false);
      } catch (err) {
        setError('Failed to load devices');
        setLoading(false);
      }
    };

    const handleError = (error: Error) => {
      setError(error.message);
      setLoading(false);
    };

    onValue(devicesRef, handleValue, handleError);

    return () => {
      off(devicesRef, 'value', handleValue);
    };
  }, []);

  return { devices, loading, error };
}

// Custom hook for real-time locations
export function useRealtimeLocations() {
  const [locations, setLocations] = useState<Record<string, LocationData>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const locationsRef = ref(db, 'locations/current');

    const handleValue = (snapshot: DataSnapshot) => {
      const data = snapshot.val();
      setLocations(data || {});
      setLoading(false);
    };

    onValue(locationsRef, handleValue);

    return () => {
      off(locationsRef, 'value', handleValue);
    };
  }, []);

  return { locations, loading };
}

// Custom hook for recent SMS
export function useRecentSms(deviceId?: string, limitCount = 50) {
  const [smsData, setSmsData] = useState<SmsData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let unsubscribe: Unsubscribe;

    const setupListener = () => {
      let q = query(
        collection(firestore, 'sms'),
        orderBy('timestamp', 'desc'),
        limit(limitCount)
      );

      // TODO: Add deviceId filter when needed
      // if (deviceId) {
      //   q = query(q, where('deviceId', '==', deviceId));
      // }

      unsubscribe = onSnapshot(q, (snapshot) => {
        const sms: SmsData[] = [];
        snapshot.forEach((doc) => {
          const data = doc.data();
          sms.push({
            deviceId: data.deviceId,
            type: data.type,
            address: data.address,
            body: data.body,
            timestamp: data.timestamp,
            contactName: data.contactName,
            isRead: data.isRead || false,
          });
        });
        setSmsData(sms);
        setLoading(false);
      });
    };

    setupListener();

    return () => {
      if (unsubscribe) {
        unsubscribe();
      }
    };
  }, [deviceId, limitCount]);

  return { smsData, loading };
}

// Custom hook for recent calls
export function useRecentCalls(deviceId?: string, limitCount = 50) {
  const [callData, setCallData] = useState<CallData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let unsubscribe: Unsubscribe;

    const setupListener = () => {
      let q = query(
        collection(firestore, 'calls'),
        orderBy('timestamp', 'desc'),
        limit(limitCount)
      );

      unsubscribe = onSnapshot(q, (snapshot) => {
        const calls: CallData[] = [];
        snapshot.forEach((doc) => {
          const data = doc.data();
          calls.push({
            deviceId: data.deviceId,
            phoneNumber: data.phone_number || data.address,
            type: data.type,
            duration: data.duration,
            timestamp: data.timestamp,
            contactName: data.contactName,
            callDirection: data.call_direction || 'inbound',
          });
        });
        setCallData(calls);
        setLoading(false);
      });
    };

    setupListener();

    return () => {
      if (unsubscribe) {
        unsubscribe();
      }
    };
  }, [deviceId, limitCount]);

  return { callData, loading };
}

// Custom hook for app usage data
export function useAppUsageData(deviceId?: string, limitCount = 100) {
  const [appUsageData, setAppUsageData] = useState<AppUsageData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let unsubscribe: Unsubscribe;

    const setupListener = () => {
      let q = query(
        collection(firestore, 'app_usage'),
        orderBy('timestamp', 'desc'),
        limit(limitCount)
      );

      unsubscribe = onSnapshot(q, (snapshot) => {
        const appUsage: AppUsageData[] = [];
        snapshot.forEach((doc) => {
          const data = doc.data();
          appUsage.push({
            deviceId: data.deviceId,
            packageName: data.package_name,
            appName: data.app_name,
            totalTimeInForeground: data.total_time_foreground || data.usage_duration_ms || 0,
            timestamp: data.timestamp,
            usageType: data.usage_type || 'statistics',
          });
        });
        setAppUsageData(appUsage);
        setLoading(false);
      });
    };

    setupListener();

    return () => {
      if (unsubscribe) {
        unsubscribe();
      }
    };
  }, [deviceId, limitCount]);

  return { appUsageData, loading };
}