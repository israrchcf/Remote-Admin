// Type definitions for the Remote Admin System

// Device-related types
export interface Device {
  deviceId: string;
  userId: string;
  status: 'online' | 'offline' | 'away';
  lastSeen: number;
  appVersion: string;
  fcmToken?: string;
  deviceModel?: string;
  androidVersion?: string;
  batteryLevel?: number;
  networkType?: 'wifi' | 'mobile' | 'offline';
  location?: {
    latitude: number;
    longitude: number;
    accuracy: number;
    timestamp: number;
  };
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
  provider?: 'gps' | 'network' | 'passive';
  address?: string;
}

// SMS data interface  
export interface SmsData {
  id?: string;
  deviceId: string;
  type: 'received' | 'sent';
  address: string;
  body: string;
  timestamp: number;
  contactName?: string;
  isRead: boolean;
  threadId?: string;
}

// Call data interface
export interface CallData {
  id?: string;
  deviceId: string;
  phoneNumber: string;
  type: 'incoming' | 'outgoing' | 'missed';
  duration: number;
  timestamp: number;
  contactName?: string;
  callDirection: 'inbound' | 'outbound';
}

// App usage data interface
export interface AppUsageData {
  id?: string;
  deviceId: string;
  packageName: string;
  appName: string;
  totalTimeInForeground: number;
  timestamp: number;
  usageType: 'statistics' | 'event';
  category?: string;
}

// Contact data interface
export interface ContactData {
  id?: string;
  deviceId: string;
  contactId: string;
  displayName: string;
  phoneNumbers?: string;
  emailAddresses?: string;
  timestamp: number;
  isStarred: boolean;
  hasPhoto: boolean;
}

// Command interface for remote device control
export interface DeviceCommand {
  id: string;
  deviceId: string;
  command: 'get_location' | 'take_photo' | 'record_audio' | 'sync_sms' | 'sync_calls' | 'restart_service';
  parameters?: Record<string, any>;
  timestamp: number;
  status: 'pending' | 'sent' | 'acknowledged' | 'completed' | 'failed';
  response?: any;
  adminId: string;
}

// Admin user interface
export interface AdminUser {
  uid: string;
  email: string;
  displayName?: string;
  role: 'admin' | 'viewer';
  lastLogin: number;
  permissions: string[];
}

// Activity feed item
export interface ActivityItem {
  id: string;
  type: 'sms' | 'call' | 'location' | 'app_usage' | 'command' | 'system';
  deviceId: string;
  timestamp: number;
  title: string;
  description: string;
  metadata?: Record<string, any>;
  severity: 'low' | 'medium' | 'high';
}

// Statistics interface
export interface DeviceStatistics {
  totalDevices: number;
  onlineDevices: number;
  offlineDevices: number;
  todaySms: number;
  todayCalls: number;
  activeDataSync: number;
  lastUpdateTime: number;
}

// Firebase real-time data hooks return types
export interface FirebaseHookResult<T> {
  data: T;
  loading: boolean;
  error: string | null;
  refetch?: () => void;
}

// Form validation schemas
export interface LoginFormData {
  email: string;
  password: string;
}

export interface CommandFormData {
  command: string;
  deviceId: string;
  parameters?: Record<string, any>;
}

// API response types
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

// Dashboard state types
export interface DashboardState {
  selectedDeviceId?: string;
  activeTab: 'overview' | 'devices' | 'activity' | 'map';
  filters: {
    dateRange?: [Date, Date];
    deviceIds?: string[];
    activityTypes?: string[];
  };
}

// Notification types
export interface NotificationPayload {
  title: string;
  body: string;
  icon?: string;
  deviceId?: string;
  type: 'alert' | 'command' | 'system';
  priority: 'low' | 'normal' | 'high';
  data?: Record<string, any>;
}

// Export utility types
export type DeviceStatus = Device['status'];
export type SmsType = SmsData['type'];
export type CallType = CallData['type'];
export type CommandType = DeviceCommand['command'];
export type CommandStatus = DeviceCommand['status'];
export type ActivityType = ActivityItem['type'];
export type NotificationType = NotificationPayload['type'];