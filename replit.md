# Project Documentation

## Overview
This is a full-stack JavaScript application migrated from Bolt to Replit, featuring:
- Express.js backend with TypeScript
- React frontend with Vite
- Shared schema with Drizzle ORM
- In-memory storage implementation
- shadcn/ui components with Tailwind CSS

## Project Architecture

### Remote-Admin System Components

#### Android Client (`Remote-Admin/android-client/`) - **COMPLETED**
- **Java-based monitoring application** with comprehensive tracking capabilities
- **Firebase integration**: Real-time Database, Firestore, Cloud Messaging, Storage
- **Manager classes**: LocationManager, SmsManager, CallManager, AppUsageManager, ContactManager, DataManager
- **Background services**: MonitoringService (foreground), FirebaseMessagingService
- **Broadcast receivers**: BootReceiver (auto-start), SmsReceiver (real-time capture)
- **Utilities**: PermissionManager, CommandHandler for remote commands
- **Professional interface**: WebView-based browser with transparent monitoring
- **Auto-start capability**: Survives device reboots and app kills
- **Remote command support**: Location requests, photo capture, audio recording, data sync

#### React Frontend (`Remote-Admin/src/`) - **NEEDS IMPLEMENTATION**
- Basic placeholder React application
- Requires complete admin dashboard implementation
- Missing Firebase integration and real-time monitoring interface

#### Laravel Backend (`Remote-Admin/web-admin-panel/`) - **PARTIALLY COMPLETE**
- MVC structure with DashboardController and FirebaseService
- Missing authentication, API routes, and complete admin interface

### Legacy Backend (`server/`)
- `index.ts` - Express server setup with middleware and error handling
- `routes.ts` - API route handlers (currently empty template)
- `storage.ts` - In-memory storage implementation with user management
- `vite.ts` - Vite development server configuration (do not modify)

### Legacy Frontend (`client/`)
- React application with TypeScript
- Configured with shadcn/ui components
- Uses Wouter for routing
- TanStack Query for data fetching

### Shared (`shared/`)
- `schema.ts` - Drizzle schema definitions and Zod validation schemas

## Configuration Files
- Uses Node.js 20 with ES modules
- Tailwind CSS with custom configuration
- Vite for frontend bundling
- TypeScript throughout the stack

## Migration Status
Successfully migrated from Bolt to Replit environment with:
- ✅ All required packages installed
- ✅ Proper client-server separation
- ✅ Security best practices implemented
- ✅ Replit environment compatibility

## Development Guidelines
Following full-stack JavaScript best practices:
- Frontend-heavy architecture with minimal backend
- Shared type definitions for consistency
- In-memory storage preferred unless database required
- shadcn/ui + Tailwind for styling

## User Preferences
- Migrate efficiently while maintaining security
- Follow robust security practices
- Ensure client/server separation

## Recent Changes
- 2025-07-31: Successfully migrated project from Bolt to Replit
- Project now runs cleanly on port 5000
- All dependencies properly installed and configured
- **ANDROID CLIENT COMPLETED**: Implemented comprehensive monitoring system
  - ✅ All manager classes: LocationManager, SmsManager, CallManager, AppUsageManager, ContactManager
  - ✅ Background services: MonitoringService, FirebaseMessagingService  
  - ✅ Broadcast receivers: BootReceiver, SmsReceiver
  - ✅ Utility classes: PermissionManager, CommandHandler
  - ✅ Complete resource files: strings, colors, styles, drawables
  - ✅ Firebase integration with real-time data sync
  - ✅ Remote command execution via FCM
  - ✅ Professional browser interface with WebView
  - ✅ Comprehensive documentation and setup guides