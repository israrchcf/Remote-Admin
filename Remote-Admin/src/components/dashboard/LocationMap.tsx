import React from 'react';
import { useRealtimeLocations, useRealtimeDevices } from '../../hooks/useRealtimeData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { formatDate } from '../../lib/utils';
import { 
  MapPin, 
  Navigation, 
  Clock,
  Smartphone
} from 'lucide-react';

interface LocationMapProps {
  selectedDeviceId?: string;
}

export function LocationMap({ selectedDeviceId }: LocationMapProps) {
  const { locations, loading } = useRealtimeLocations();
  const { devices } = useRealtimeDevices();

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <MapPin className="h-5 w-5 mr-2" />
            Device Locations
          </CardTitle> 
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  const locationList = Object.entries(locations);
  const filteredLocations = selectedDeviceId 
    ? locationList.filter(([deviceId]) => deviceId === selectedDeviceId)
    : locationList;

  if (filteredLocations.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <MapPin className="h-5 w-5 mr-2" />
            Device Locations
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <Navigation className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No location data available</p>
            <p className="text-sm mt-1">
              {selectedDeviceId 
                ? 'No location data for selected device'
                : 'Location data will appear here when devices report their position'
              }
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Map Placeholder */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <MapPin className="h-5 w-5 mr-2" />
            Live Location Map
            {selectedDeviceId && (
              <span className="ml-2 text-sm font-normal text-gray-500">
                (Device: {selectedDeviceId.substring(0, 8)}...)
              </span>
            )}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="bg-gray-100 rounded-lg p-8 text-center">
            <MapPin className="h-16 w-16 mx-auto mb-4 text-gray-400" />
            <p className="text-gray-600 mb-2">Interactive Map View</p>
            <p className="text-sm text-gray-500">
              Google Maps integration would be implemented here to show device locations in real-time
            </p>
            <div className="mt-4 flex justify-center space-x-4">
              {filteredLocations.slice(0, 3).map(([deviceId, location]) => (
                <div key={deviceId} className="text-xs bg-white p-2 rounded shadow">
                  <div className="font-medium">Device {deviceId.substring(0, 8)}</div>
                  <div className="text-gray-500">
                    {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Location Details */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Navigation className="h-5 w-5 mr-2" />
            Location Details ({filteredLocations.length})
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          <div className="divide-y divide-gray-200">
            {filteredLocations.map(([deviceId, location]) => {
              const device = devices[deviceId];
              
              return (
                <div key={deviceId} className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center">
                        <Smartphone className="h-4 w-4 text-gray-400 mr-2" />
                        <h3 className="text-sm font-medium text-gray-900">
                          {device?.deviceModel || `Device ${deviceId.substring(0, 8)}`}
                        </h3>
                      </div>
                      
                      <div className="mt-2 space-y-1">
                        <div className="flex items-center text-sm text-gray-600">
                          <MapPin className="h-3 w-3 mr-1" />
                          <span>
                            {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
                          </span>
                        </div>
                        
                        <div className="flex items-center text-sm text-gray-500">
                          <Clock className="h-3 w-3 mr-1" />
                          <span>{formatDate(location.timestamp)}</span>
                        </div>
                        
                        <div className="text-xs text-gray-500">
                          Accuracy: ±{Math.round(location.accuracy)}m
                          {location.altitude && (
                            <span className="ml-3">
                              Altitude: {Math.round(location.altitude)}m
                            </span>
                          )}
                          {location.speed && location.speed > 0 && (
                            <span className="ml-3">
                              Speed: {Math.round(location.speed * 3.6)}km/h
                            </span>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-col items-end space-y-1">
                      <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                        Active
                      </span>
                      <span className="text-xs text-gray-400">
                        {location.provider || 'GPS'}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}