import { z } from 'zod';

// Email validation regex
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Phone number validation regex (international format)
const phoneRegex = /^\+?[1-9]\d{1,14}$/;

// Device ID validation (Firebase-style)
const deviceIdRegex = /^[a-zA-Z0-9_-]+$/;

// Common validation schemas
export const ValidationSchemas = {
  email: z.string().regex(emailRegex, 'Please enter a valid email address'),
  
  password: z.string()
    .min(6, 'Password must be at least 6 characters')
    .max(128, 'Password must be less than 128 characters'),
    
  deviceId: z.string()
    .min(3, 'Device ID must be at least 3 characters')
    .regex(deviceIdRegex, 'Device ID can only contain letters, numbers, hyphens, and underscores'),
    
  phoneNumber: z.string()
    .regex(phoneRegex, 'Please enter a valid phone number'),
    
  timestamp: z.number()
    .min(0, 'Timestamp must be positive')
    .max(Date.now() + 86400000, 'Timestamp cannot be in the future'), // Allow 1 day ahead
    
  coordinates: z.object({
    latitude: z.number().min(-90).max(90),
    longitude: z.number().min(-180).max(180),
    accuracy: z.number().min(0),
  }),
  
  // Form validation schemas
  loginForm: z.object({
    email: z.string().regex(emailRegex, 'Please enter a valid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
  }),
  
  commandForm: z.object({
    command: z.enum(['get_location', 'take_photo', 'record_audio', 'sync_sms', 'sync_calls', 'restart_service']),
    deviceId: z.string().min(3, 'Please select a device'),
    parameters: z.record(z.any()).optional(),
  }),
  
  // Data validation schemas
  deviceData: z.object({
    deviceId: z.string().min(3),
    userId: z.string().min(3),
    status: z.enum(['online', 'offline', 'away']),
    lastSeen: z.number().min(0),
    appVersion: z.string().min(1),
    fcmToken: z.string().optional(),
    deviceModel: z.string().optional(),
    androidVersion: z.string().optional(),
    batteryLevel: z.number().min(0).max(100).optional(),
    networkType: z.enum(['wifi', 'mobile', 'offline']).optional(),
  }),
  
  locationData: z.object({
    deviceId: z.string().min(3),
    latitude: z.number().min(-90).max(90),
    longitude: z.number().min(-180).max(180),
    accuracy: z.number().min(0),
    timestamp: z.number().min(0),
    altitude: z.number().optional(),
    speed: z.number().min(0).optional(),
    provider: z.enum(['gps', 'network', 'passive']).optional(),
    address: z.string().optional(),
  }),
  
  smsData: z.object({
    deviceId: z.string().min(3),
    type: z.enum(['received', 'sent']),
    address: z.string().min(1),
    body: z.string().max(2000), // SMS character limit
    timestamp: z.number().min(0),
    contactName: z.string().optional(),
    isRead: z.boolean(),
    threadId: z.string().optional(),
  }),
  
  callData: z.object({
    deviceId: z.string().min(3),
    phoneNumber: z.string().min(1),
    type: z.enum(['incoming', 'outgoing', 'missed']),
    duration: z.number().min(0),
    timestamp: z.number().min(0),
    contactName: z.string().optional(),
    callDirection: z.enum(['inbound', 'outbound']),
  }),
};

// Validation helper functions
export const ValidationHelpers = {
  isValidEmail: (email: string): boolean => {
    return emailRegex.test(email);
  },
  
  isValidPhoneNumber: (phone: string): boolean => {
    return phoneRegex.test(phone);
  },
  
  isValidDeviceId: (deviceId: string): boolean => {
    return deviceIdRegex.test(deviceId) && deviceId.length >= 3;
  },
  
  isValidTimestamp: (timestamp: number): boolean => {
    return timestamp > 0 && timestamp <= Date.now() + 86400000; // Allow 1 day ahead
  },
  
  isValidCoordinates: (lat: number, lng: number): boolean => {
    return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
  },
  
  sanitizeString: (str: string, maxLength = 1000): string => {
    return str.trim().slice(0, maxLength);
  },
  
  sanitizePhoneNumber: (phone: string): string => {
    // Remove all non-digit characters except +
    return phone.replace(/[^\d+]/g, '');
  },
  
  validateAndSanitizeInput: <T>(data: unknown, schema: z.ZodSchema<T>): T => {
    try {
      return schema.parse(data);
    } catch (error) {
      if (error instanceof z.ZodError) {
        throw new Error(`Validation failed: ${error.errors.map(e => e.message).join(', ')}`);
      }
      throw error;
    }
  },
};

// Custom validation errors
export class ValidationError extends Error {
  constructor(message: string, public field?: string) {
    super(message);
    this.name = 'ValidationError';
  }
}

// Data sanitization functions
export const Sanitizers = {
  smsBody: (body: string): string => {
    return body.trim().slice(0, 2000); // SMS character limit
  },
  
  contactName: (name: string): string => {
    return name.trim().slice(0, 100).replace(/[<>\"']/g, ''); // Remove potential XSS characters
  },
  
  deviceModel: (model: string): string => {
    return model.trim().slice(0, 50).replace(/[<>\"']/g, '');
  },
  
  appName: (name: string): string => {
    return name.trim().slice(0, 100).replace(/[<>\"']/g, '');
  },
};