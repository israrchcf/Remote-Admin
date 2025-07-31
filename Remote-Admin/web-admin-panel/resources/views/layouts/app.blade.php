<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="Remote Android Monitoring & Administration Panel">
    <meta name="author" content="Remote Admin">
    <meta name="csrf-token" content="{{ csrf_token() }}">

    <title>@yield('title', 'Dashboard') - Remote Admin Panel</title>

    <!-- Custom fonts for this template-->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css?family=Nunito:200,200i,300,300i,400,400i,600,600i,700,700i,800,800i,900,900i" rel="stylesheet">

    <!-- Custom styles for this template-->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/startbootstrap-sb-admin-2/4.1.4/css/sb-admin-2.min.css" rel="stylesheet">
    
    <!-- DataTables -->
    <link href="https://cdn.datatables.net/1.13.7/css/dataTables.bootstrap4.min.css" rel="stylesheet">
    
    <!-- Custom CSS -->
    <style>
        .sidebar-brand-icon i {
            font-size: 2rem;
        }
        
        .card {
            box-shadow: 0 0.15rem 1.75rem 0 rgba(58, 59, 69, 0.15);
        }
        
        .border-left-primary {
            border-left: 0.25rem solid #4e73df !important;
        }
        
        .border-left-success {
            border-left: 0.25rem solid #1cc88a !important;
        }
        
        .border-left-warning {
            border-left: 0.25rem solid #f6c23e !important;
        }
        
        .border-left-info {
            border-left: 0.25rem solid #36b9cc !important;
        }
        
        .bg-gradient-primary {
            background: linear-gradient(180deg, #4e73df 10%, #224abe 100%);
        }
        
        .real-time-indicator {
            position: relative;
            display: inline-block;
        }
        
        .real-time-indicator::after {
            content: '';
            position: absolute;
            top: 0;
            right: -5px;
            width: 8px;
            height: 8px;
            background: #28a745;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.7); }
            70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(40, 167, 69, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(40, 167, 69, 0); }
        }
        
        .device-status-online {
            color: #28a745;
        }
        
        .device-status-offline {
            color: #6c757d;
        }
        
        .activity-timeline {
            position: relative;
            padding-left: 1.5rem;
        }
        
        .activity-timeline::before {
            content: '';
            position: absolute;
            left: 0.75rem;
            top: 0;
            bottom: 0;
            width: 2px;
            background: #e3e6f0;
        }
        
        .activity-item {
            position: relative;
            margin-bottom: 1rem;
        }
        
        .activity-item::before {
            content: '';
            position: absolute;
            left: -0.875rem;
            top: 0.5rem;
            width: 0.75rem;
            height: 0.75rem;
            background: #4e73df;
            border-radius: 50%;
            border: 2px solid #fff;
        }
    </style>
    
    @yield('styles')
</head>

<body id="page-top">

    <!-- Page Wrapper -->
    <div id="wrapper">

        <!-- Sidebar -->
        <ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

            <!-- Sidebar - Brand -->
            <a class="sidebar-brand d-flex align-items-center justify-content-center" href="{{ route('dashboard') }}">
                <div class="sidebar-brand-icon rotate-n-15">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <div class="sidebar-brand-text mx-3">Remote Admin</div>
            </a>

            <!-- Divider -->
            <hr class="sidebar-divider my-0">

            <!-- Nav Item - Dashboard -->
            <li class="nav-item {{ request()->routeIs('dashboard') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('dashboard') }}">
                    <i class="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </a>
            </li>

            <!-- Divider -->
            <hr class="sidebar-divider">

            <!-- Heading -->
            <div class="sidebar-heading">
                Monitoring
            </div>

            <!-- Nav Item - Devices -->
            <li class="nav-item {{ request()->routeIs('devices.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('devices.index') }}">
                    <i class="fas fa-fw fa-mobile-alt"></i>
                    <span>Devices</span>
                </a>
            </li>

            <!-- Nav Item - Locations -->
            <li class="nav-item {{ request()->routeIs('locations.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('locations.index') }}">
                    <i class="fas fa-fw fa-map-marker-alt"></i>
                    <span>Location Tracking</span>
                </a>
            </li>

            <!-- Nav Item - Communications -->
            <li class="nav-item {{ request()->routeIs('communications.*') ? 'active' : '' }}">
                <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseCommunications"
                    aria-expanded="true" aria-controls="collapseCommunications">
                    <i class="fas fa-fw fa-comments"></i>
                    <span>Communications</span>
                </a>
                <div id="collapseCommunications" class="collapse {{ request()->routeIs('communications.*') ? 'show' : '' }}" 
                     aria-labelledby="headingTwo" data-parent="#accordionSidebar">
                    <div class="bg-white py-2 collapse-inner rounded">
                        <h6 class="collapse-header">Communication Logs:</h6>
                        <a class="collapse-item {{ request()->routeIs('communications.sms') ? 'active' : '' }}" 
                           href="{{ route('communications.sms') }}">SMS Messages</a>
                        <a class="collapse-item {{ request()->routeIs('communications.calls') ? 'active' : '' }}" 
                           href="{{ route('communications.calls') }}">Call Logs</a>
                    </div>
                </div>
            </li>

            <!-- Nav Item - Media -->
            <li class="nav-item {{ request()->routeIs('media.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('media.index') }}">
                    <i class="fas fa-fw fa-images"></i>
                    <span>Media Files</span>
                </a>
            </li>

            <!-- Nav Item - Apps -->
            <li class="nav-item {{ request()->routeIs('apps.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('apps.index') }}">
                    <i class="fas fa-fw fa-mobile-alt"></i>
                    <span>App Usage</span>
                </a>
            </li>

            <!-- Divider -->
            <hr class="sidebar-divider">

            <!-- Heading -->
            <div class="sidebar-heading">
                Management
            </div>

            <!-- Nav Item - Commands -->
            <li class="nav-item {{ request()->routeIs('commands.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('commands.index') }}">
                    <i class="fas fa-fw fa-terminal"></i>
                    <span>Remote Commands</span>
                </a>
            </li>

            <!-- Nav Item - Reports -->
            <li class="nav-item {{ request()->routeIs('reports.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('reports.index') }}">
                    <i class="fas fa-fw fa-chart-area"></i>
                    <span>Reports & Analytics</span>
                </a>
            </li>

            <!-- Nav Item - Settings -->
            <li class="nav-item {{ request()->routeIs('settings.*') ? 'active' : '' }}">
                <a class="nav-link" href="{{ route('settings.index') }}">
                    <i class="fas fa-fw fa-cogs"></i>
                    <span>Settings</span>
                </a>
            </li>

            <!-- Divider -->
            <hr class="sidebar-divider d-none d-md-block">

            <!-- Sidebar Toggler (Sidebar) -->
            <div class="text-center d-none d-md-inline">
                <button class="rounded-circle border-0" id="sidebarToggle"></button>
            </div>

        </ul>
        <!-- End of Sidebar -->

        <!-- Content Wrapper -->
        <div id="content-wrapper" class="d-flex flex-column">

            <!-- Main Content -->
            <div id="content">

                <!-- Topbar -->
                <nav class="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">

                    <!-- Sidebar Toggle (Topbar) -->
                    <button id="sidebarToggleTop" class="btn btn-link d-md-none rounded-circle mr-3">
                        <i class="fa fa-bars"></i>
                    </button>

                    <!-- Topbar Search -->
                    <form class="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                        <div class="input-group">
                            <input type="text" class="form-control bg-light border-0 small" placeholder="Search devices..."
                                aria-label="Search" aria-describedby="basic-addon2">
                            <div class="input-group-append">
                                <button class="btn btn-primary" type="button">
                                    <i class="fas fa-search fa-sm"></i>
                                </button>
                            </div>
                        </div>
                    </form>

                    <!-- Topbar Navbar -->
                    <ul class="navbar-nav ml-auto">

                        <!-- Nav Item - Search Dropdown (Visible Only XS) -->
                        <li class="nav-item dropdown no-arrow d-sm-none">
                            <a class="nav-link dropdown-toggle" href="#" id="searchDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-search fa-fw"></i>
                            </a>
                        </li>

                        <!-- Nav Item - Alerts -->
                        <li class="nav-item dropdown no-arrow mx-1">
                            <a class="nav-link dropdown-toggle" href="#" id="alertsDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-bell fa-fw"></i>
                                <!-- Counter - Alerts -->
                                <span class="badge badge-danger badge-counter" id="alertCounter">3+</span>
                            </a>
                            <div class="dropdown-list dropdown-menu dropdown-menu-right shadow animated--grow-in"
                                aria-labelledby="alertsDropdown">
                                <h6 class="dropdown-header">
                                    System Alerts
                                </h6>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="mr-3">
                                        <div class="icon-circle bg-primary">
                                            <i class="fas fa-mobile-alt text-white"></i>
                                        </div>
                                    </div>
                                    <div>
                                        <div class="small text-gray-500">December 12, 2023</div>
                                        <span class="font-weight-bold">New device connected</span>
                                    </div>
                                </a>
                                <a class="dropdown-item d-flex align-items-center" href="#">
                                    <div class="mr-3">
                                        <div class="icon-circle bg-warning">
                                            <i class="fas fa-exclamation-triangle text-white"></i>
                                        </div>
                                    </div>
                                    <div>
                                        <div class="small text-gray-500">December 12, 2023</div>
                                        Device offline for extended period
                                    </div>
                                </a>
                                <a class="dropdown-item text-center small text-gray-500" href="#">Show All Alerts</a>
                            </div>
                        </li>

                        <!-- Nav Item - Messages -->
                        <li class="nav-item dropdown no-arrow mx-1">
                            <a class="nav-link dropdown-toggle" href="#" id="messagesDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <i class="fas fa-envelope fa-fw"></i>
                                <!-- Counter - Messages -->
                                <span class="badge badge-danger badge-counter" id="messageCounter">7</span>
                            </a>
                        </li>

                        <div class="topbar-divider d-none d-sm-block"></div>

                        <!-- Nav Item - User Information -->
                        <li class="nav-item dropdown no-arrow">
                            <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                <span class="mr-2 d-none d-lg-inline text-gray-600 small">{{ auth()->user()->name ?? 'Admin' }}</span>
                                <img class="img-profile rounded-circle"
                                    src="https://ui-avatars.com/api/?name={{ urlencode(auth()->user()->name ?? 'Admin') }}&background=4e73df&color=fff">
                            </a>
                            <!-- Dropdown - User Information -->
                            <div class="dropdown-menu dropdown-menu-right shadow animated--grow-in"
                                aria-labelledby="userDropdown">
                                <a class="dropdown-item" href="{{ route('profile.edit') }}">
                                    <i class="fas fa-user fa-sm fa-fw mr-2 text-gray-400"></i>
                                    Profile
                                </a>
                                <a class="dropdown-item" href="{{ route('settings.index') }}">
                                    <i class="fas fa-cogs fa-sm fa-fw mr-2 text-gray-400"></i>
                                    Settings
                                </a>
                                <a class="dropdown-item" href="#">
                                    <i class="fas fa-list fa-sm fa-fw mr-2 text-gray-400"></i>
                                    Activity Log
                                </a>
                                <div class="dropdown-divider"></div>
                                <a class="dropdown-item" href="#" data-toggle="modal" data-target="#logoutModal">
                                    <i class="fas fa-sign-out-alt fa-sm fa-fw mr-2 text-gray-400"></i>
                                    Logout
                                </a>
                            </div>
                        </li>

                    </ul>

                </nav>
                <!-- End of Topbar -->

                <!-- Begin Page Content -->
                @yield('content')
                <!-- /.container-fluid -->

            </div>
            <!-- End of Main Content -->

            <!-- Footer -->
            <footer class="sticky-footer bg-white">
                <div class="container my-auto">
                    <div class="copyright text-center my-auto">
                        <span>Copyright &copy; Remote Admin Panel {{ date('Y') }}</span>
                    </div>
                </div>
            </footer>
            <!-- End of Footer -->

        </div>
        <!-- End of Content Wrapper -->

    </div>
    <!-- End of Page Wrapper -->

    <!-- Scroll to Top Button-->
    <a class="scroll-to-top rounded" href="#page-top">
        <i class="fas fa-angle-up"></i>
    </a>

    <!-- Logout Modal-->
    <div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
        aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLabel">Ready to Leave?</h5>
                    <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">×</span>
                    </button>
                </div>
                <div class="modal-body">Select "Logout" below if you are ready to end your current session.</div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                    <form method="POST" action="{{ route('logout') }}" class="d-inline">
                        @csrf
                        <button class="btn btn-primary" type="submit">Logout</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap core JavaScript-->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/4.6.2/js/bootstrap.bundle.min.js"></script>

    <!-- Core plugin JavaScript-->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.4.1/jquery.easing.min.js"></script>

    <!-- Custom scripts for all pages-->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/startbootstrap-sb-admin-2/4.1.4/js/sb-admin-2.min.js"></script>

    <!-- DataTables -->
    <script src="https://cdn.datatables.net/1.13.7/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.7/js/dataTables.bootstrap4.min.js"></script>

    <!-- Global JavaScript -->
    <script>
        // Initialize DataTables
        $(document).ready(function() {
            $('#devicesTable').DataTable({
                responsive: true,
                order: [[2, 'desc']], // Sort by last seen
                pageLength: 25
            });
        });

        // Real-time updates via polling
        function startRealTimeUpdates() {
            setInterval(function() {
                updateOnlineDeviceCount();
                updateRecentActivities();
            }, 30000); // Update every 30 seconds
        }

        function updateOnlineDeviceCount() {
            fetch('/api/devices/count')
                .then(response => response.json())
                .then(data => {
                    // Update counters if they exist
                    const onlineCounter = document.querySelector('.online-devices-count');
                    if (onlineCounter) {
                        onlineCounter.textContent = data.online;
                    }
                })
                .catch(error => console.error('Error updating device count:', error));
        }

        function updateRecentActivities() {
            // Implementation would fetch and update recent activities
            // This is a placeholder for real-time activity updates
        }

        // Initialize real-time updates
        $(document).ready(function() {
            startRealTimeUpdates();
        });

        // Global notification system
        function showNotification(message, type = 'info') {
            const alertClass = type === 'error' ? 'alert-danger' : 
                             type === 'success' ? 'alert-success' : 
                             type === 'warning' ? 'alert-warning' : 'alert-info';
            
            const notification = $(`
                <div class="alert ${alertClass} alert-dismissible fade show" role="alert" style="position: fixed; top: 20px; right: 20px; z-index: 9999;">
                    ${message}
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
            `);
            
            $('body').append(notification);
            
            // Auto-dismiss after 5 seconds
            setTimeout(function() {
                notification.alert('close');
            }, 5000);
        }

        // CSRF token setup for AJAX
        $.ajaxSetup({
            headers: {
                'X-CSRF-TOKEN': $('meta[name="csrf-token"]').attr('content')
            }
        });
    </script>

    @yield('scripts')

</body>

</html>