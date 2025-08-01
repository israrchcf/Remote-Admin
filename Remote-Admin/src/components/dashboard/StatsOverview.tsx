import React from 'react';
import { useRealtimeDevices, useRecentSms, useRecentCalls } from '../../hooks/useRealtimeData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { 
  Smartphone, 
  MessageSquare, 
  Phone, 
  MapPin,
  TrendingUp,
  Activity
} from 'lucide-react';

export function StatsOverview() {
  const { devices, loading: devicesLoading } = useRealtimeDevices();
  const { smsData, loading: smsLoading } = useRecentSms(undefined, 100);
  const { callData, loading: callLoading } = useRecentCalls(undefined, 100);

  // Calculate stats
  const deviceList = Object.entries(devices);
  const onlineDevices = deviceList.filter(([_, device]) => {
    const timeDiff = Date.now() - device.lastSeen;
    return timeDiff < 300000; // Online if seen within 5 minutes
  }).length;

  const todaySms = smsData.filter(sms => {
    const today = new Date();
    const smsDate = new Date(sms.timestamp);
    return smsDate.toDateString() === today.toDateString();
  }).length;

  const todayCalls = callData.filter(call => {
    const today = new Date();
    const callDate = new Date(call.timestamp);
    return callDate.toDateString() === today.toDateString();
  }).length;

  const stats = [
    {
      title: 'Total Devices',
      value: devicesLoading ? '...' : deviceList.length.toString(),
      subtitle: `${onlineDevices} online`,
      icon: <Smartphone className="h-8 w-8 text-blue-600" />,
      color: 'bg-blue-50',
    },
    {
      title: 'SMS Today',
      value: smsLoading ? '...' : todaySms.toString(),
      subtitle: `${smsData.length} total`,
      icon: <MessageSquare className="h-8 w-8 text-green-600" />,
      color: 'bg-green-50',
    },
    {
      title: 'Calls Today', 
      value: callLoading ? '...' : todayCalls.toString(),
      subtitle: `${callData.length} total`,
      icon: <Phone className="h-8 w-8 text-purple-600" />,
      color: 'bg-purple-50',
    },
    {
      title: 'Active Locations',
      value: devicesLoading ? '...' : onlineDevices.toString(),
      subtitle: 'GPS tracking',
      icon: <MapPin className="h-8 w-8 text-red-600" />,
      color: 'bg-red-50',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {stats.map((stat, index) => (
        <Card key={index}>
          <CardContent className="p-6">
            <div className="flex items-center">
              <div className={`p-3 rounded-lg ${stat.color}`}>
                {stat.icon}
              </div>
              <div className="ml-4 flex-1">
                <p className="text-sm font-medium text-gray-600">
                  {stat.title}
                </p>
                <div className="flex items-baseline">
                  <p className="text-2xl font-semibold text-gray-900">
                    {stat.value}
                  </p>
                  {stat.subtitle && (
                    <p className="ml-2 text-sm text-gray-500">
                      {stat.subtitle}
                    </p>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}