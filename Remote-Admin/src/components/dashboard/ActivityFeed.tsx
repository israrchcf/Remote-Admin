import React from 'react';
import { useRecentSms, useRecentCalls } from '../../hooks/useRealtimeData';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { formatDate, truncateText } from '../../lib/utils';
import { 
  MessageSquare, 
  Phone, 
  PhoneCall, 
  PhoneIncoming, 
  PhoneOutgoing,
  PhoneMissed,
  Clock
} from 'lucide-react';

interface ActivityFeedProps {
  deviceId?: string;
}

interface ActivityItem {
  id: string;
  type: 'sms' | 'call';
  timestamp: number;
  description: string;
  details: string;
  icon: React.ReactNode;
  color: string;
}

export function ActivityFeed({ deviceId }: ActivityFeedProps) {
  const { smsData, loading: smsLoading } = useRecentSms(deviceId, 25);
  const { callData, loading: callLoading } = useRecentCalls(deviceId, 25);

  const loading = smsLoading || callLoading;

  // Combine and sort activities
  const activities: ActivityItem[] = React.useMemo(() => {
    const items: ActivityItem[] = [];

    // Add SMS activities
    smsData.forEach((sms, index) => {
      items.push({
        id: `sms-${index}-${sms.timestamp}`,
        type: 'sms',
        timestamp: sms.timestamp,
        description: `${sms.type === 'received' ? 'Received' : 'Sent'} SMS`,
        details: `${sms.contactName || sms.address}: ${truncateText(sms.body, 100)}`,
        icon: <MessageSquare className="h-4 w-4" />,
        color: sms.type === 'received' ? 'text-blue-600' : 'text-green-600',
      });
    });

    // Add call activities
    callData.forEach((call, index) => {
      const getCallIcon = () => {
        switch (call.type) {
          case 'incoming':
            return <PhoneIncoming className="h-4 w-4" />;
          case 'outgoing':
            return <PhoneOutgoing className="h-4 w-4" />;
          case 'missed':
            return <PhoneMissed className="h-4 w-4" />;
          default:
            return <Phone className="h-4 w-4" />;
        }
      };

      const getCallColor = () => {
        switch (call.type) {
          case 'incoming':
            return 'text-blue-600';
          case 'outgoing':
            return 'text-green-600';
          case 'missed':
            return 'text-red-600';
          default:
            return 'text-gray-600';
        }
      };

      const formatDuration = (duration: number) => {
        if (duration === 0) return 'No answer';
        const minutes = Math.floor(duration / 60);
        const seconds = duration % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
      };

      items.push({
        id: `call-${index}-${call.timestamp}`,
        type: 'call',
        timestamp: call.timestamp,
        description: `${call.type.charAt(0).toUpperCase() + call.type.slice(1)} Call`,
        details: `${call.contactName || call.phoneNumber} • ${formatDuration(call.duration)}`,
        icon: getCallIcon(),
        color: getCallColor(),
      });
    });

    // Sort by timestamp (newest first)
    return items.sort((a, b) => b.timestamp - a.timestamp);
  }, [smsData, callData]);

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (activities.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <Clock className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No recent activity</p>
            <p className="text-sm mt-1">Activity will appear here as it happens</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Clock className="h-5 w-5 mr-2" />
          Recent Activity ({activities.length})
        </CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="max-h-96 overflow-y-auto">
          <div className="divide-y divide-gray-200">
            {activities.map((activity) => (
              <div key={activity.id} className="p-4">
                <div className="flex items-start">
                  <div className={`flex-shrink-0 ${activity.color} mt-0.5`}>
                    {activity.icon}
                  </div>
                  <div className="ml-3 flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-medium text-gray-900">
                        {activity.description}
                      </p>
                      <p className="text-xs text-gray-500">
                        {formatDate(activity.timestamp)}
                      </p>
                    </div>
                    <p className="text-sm text-gray-600 mt-1">
                      {activity.details}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}