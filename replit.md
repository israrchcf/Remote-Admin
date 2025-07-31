# Project Documentation

## Overview
This is a full-stack JavaScript application migrated from Bolt to Replit, featuring:
- Express.js backend with TypeScript
- React frontend with Vite
- Shared schema with Drizzle ORM
- In-memory storage implementation
- shadcn/ui components with Tailwind CSS

## Project Architecture

### Backend (`server/`)
- `index.ts` - Express server setup with middleware and error handling
- `routes.ts` - API route handlers (currently empty template)
- `storage.ts` - In-memory storage implementation with user management
- `vite.ts` - Vite development server configuration (do not modify)

### Frontend (`client/`)
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