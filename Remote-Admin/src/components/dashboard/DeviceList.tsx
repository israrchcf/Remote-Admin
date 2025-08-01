import React from 'react';
import { useRealtimeDevices, useRealtimeLocations } from '../../hooks/useRealtimeData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { getDeviceStatus, formatDate } from '../../lib/utils';
import { 
  Smartphone, 
  MapPin, 
  Clock, 
  Wifi, 
  WifiOff,
  AlertCircle 
} from 'lucide-react';

interface DeviceListProps {
  onDeviceSelect?: (deviceId: string) => void;
  selectedDeviceId?: string;
}

export function DeviceList({ onDeviceSelect, selectedDeviceId }: DeviceListProps) {
  const { devices, loading, error } = useRealtimeDevices();
  const { locations } = useRealtimeLocations();

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Connected Devices</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Connected Devices</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center text-red-600 py-4">
            <AlertCircle className="h-5 w-5 mr-2" />
            <span>Error loading devices: {error}</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  const deviceList = Object.entries(devices);

  if (deviceList.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Connected Devices</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <Smartphone className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No devices connected</p>
            <p className="text-sm mt-1">Devices will appear here once they connect</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Smartphone className="h-5 w-5 mr-2" />
          Connected Devices ({deviceList.length})
        </CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="divide-y divide-gray-200">
          {deviceList.map(([deviceId, device]) => {
            const status = getDeviceStatus(device.lastSeen);
            const location = locations[deviceId];
            const isSelected = selectedDeviceId === deviceId;

            return (
              <div
                key={deviceId}
                className={`p-4 hover:bg-gray-50 cursor-pointer transition-colors ${
                  isSelected ? 'bg-blue-50 border-l-4 border-blue-500' : ''
                }`}
                onClick={() => onDeviceSelect?.(deviceId)}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center">
                      <div className={`h-3 w-3 rounded-full mr-3 ${
                        status.status === 'online' ? 'bg-green-500' :
                        status.status === 'away' ? 'bg-yellow-500' : 'bg-red-500'
                      }`} />
                      <h3 className="text-sm font-medium text-gray-900 truncate">
                        {device.deviceModel || `Device ${deviceId.substring(0, 8)}`}
                      </h3>
                    </div>
                    
                    <div className="mt-1 flex items-center text-xs text-gray-500">
                      <span className="truncate">ID: {deviceId}</span>
                    </div>

                    <div className="mt-2 flex items-center space-x-4 text-xs text-gray-500">
                      <div className="flex items-center">
                        {status.status === 'online' ? 
                          <Wifi className="h-3 w-3 mr-1" /> : 
                          <WifiOff className="h-3 w-3 mr-1" />
                        }
                        <span>{status.text}</span>
                      </div>
                      
                      <div className="flex items-center">
                        <Clock className="h-3 w-3 mr-1" />
                        <span>{formatDate(device.lastSeen)}</span>
                      </div>
                      
                      {location && (
                        <div className="flex items-center">
                          <MapPin className="h-3 w-3 mr-1" />
                          <span>
                            {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
                          </span>
                        </div>
                      )}
                    </div>

                    {device.androidVersion && (
                      <div className="mt-1 text-xs text-gray-400">
                        Android {device.androidVersion} • App v{device.appVersion}
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col items-end space-y-1">
                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      status.status === 'online' ? 'bg-green-100 text-green-800' :
                      status.status === 'away' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {status.text}
                    </span>
                    
                    {location && (
                      <span className="text-xs text-gray-400">
                        ±{Math.round(location.accuracy)}m
                      </span>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}