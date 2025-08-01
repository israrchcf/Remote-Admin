import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { DeviceList } from './DeviceList';
import { ActivityFeed } from './ActivityFeed';
import { CommandCenter } from './CommandCenter';
import { StatsOverview } from './StatsOverview';
import { LocationMap } from './LocationMap';
import { Button } from '../ui/Button';
import { 
  LogOut, 
  Shield, 
  Users, 
  Activity,
  Settings,
  Bell
} from 'lucide-react';

export function Dashboard() {
  const { user, logout } = useAuth();
  const [selectedDeviceId, setSelectedDeviceId] = useState<string>();
  const [activeTab, setActiveTab] = useState<'overview' | 'devices' | 'activity' | 'map'>('overview');

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('Failed to logout:', error);
    }
  };

  const handleCommandSent = (command: string, deviceId: string) => {
    console.log(`Command ${command} sent to device ${deviceId}`);
    // You could show a toast notification here
  };

  const tabs = [
    { id: 'overview', name: 'Overview', icon: <Activity className="h-4 w-4" /> },
    { id: 'devices', name: 'Devices', icon: <Users className="h-4 w-4" /> },
    { id: 'activity', name: 'Activity', icon: <Bell className="h-4 w-4" /> },
    { id: 'map', name: 'Map', icon: <Shield className="h-4 w-4" /> },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Shield className="h-8 w-8 text-blue-600 mr-3" />
              <div>
                <h1 className="text-xl font-semibold text-gray-900">
                  Remote Admin Panel
                </h1>
                <p className="text-sm text-gray-500">
                  Device Monitoring Dashboard
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-700">
                Welcome, {user?.displayName || user?.email}
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="flex items-center"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation Tabs */}
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex items-center px-1 py-4 text-sm font-medium border-b-2 transition-colors ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.name}
              </button>
            ))}
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'overview' && (
          <div className="space-y-8">
            {/* Stats Overview */}
            <StatsOverview />
            
            {/* Two Column Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <div className="lg:col-span-2">
                <DeviceList 
                  onDeviceSelect={setSelectedDeviceId}
                  selectedDeviceId={selectedDeviceId}
                />
              </div>
              <div>
                <CommandCenter 
                  selectedDeviceId={selectedDeviceId}
                  onCommandSent={handleCommandSent}
                />
              </div>
            </div>

            {/* Activity Feed */}
            <ActivityFeed deviceId={selectedDeviceId} />
          </div>
        )}

        {activeTab === 'devices' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2">
              <DeviceList 
                onDeviceSelect={setSelectedDeviceId}
                selectedDeviceId={selectedDeviceId}
              />
            </div>
            <div>
              <CommandCenter 
                selectedDeviceId={selectedDeviceId}
                onCommandSent={handleCommandSent}
              />
            </div>
          </div>
        )}

        {activeTab === 'activity' && (
          <div className="space-y-8">
            <ActivityFeed deviceId={selectedDeviceId} />
          </div>
        )}

        {activeTab === 'map' && (
          <div className="space-y-8">
            <LocationMap selectedDeviceId={selectedDeviceId} />
          </div>
        )}
      </main>
    </div>
  );
}