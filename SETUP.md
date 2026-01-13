# Quick Setup Guide

## Backend Setup

1. **Database**: 
   - Create MySQL database
   - Run `database/schema.sql`
   - Update `src/main/resources/application.yml` with your MySQL credentials

2. **Build & Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   Backend runs on `http://localhost:8080`

## Frontend Setup

1. **Install Dependencies**:
   ```bash
   cd frontend
   npm install
   ```

2. **Run Development Server**:
   ```bash
   npm run dev
   ```
   Frontend runs on `http://localhost:5173`

## Default Credentials

Register a new account through the login page, or you can manually insert a user in the database.

## Testing the Application

1. Start the backend (port 8080)
2. Start the frontend (port 5173)
3. Open `http://localhost:5173` in your browser
4. Register/Login
5. Navigate to a board (you'll need to create one via API or database)

## Creating Your First Board

You can create a board via API:
```bash
POST http://localhost:8080/api/boards
Headers: Authorization: Bearer <your-jwt-token>
Body: {
  "name": "My First Board",
  "description": "Test board",
  "workspaceId": 1
}
```

Note: You'll need to create a workspace first, or modify the database to have a workspace with ID 1.

## Architecture Summary

✅ **Backend**: Spring Boot with JWT Auth, WebSocket (STOMP), MySQL
✅ **Frontend**: React 18, TypeScript, Zustand, React Query, Drag & Drop
✅ **Real-time**: WebSocket broadcasting for card movements
✅ **Optimistic Updates**: UI updates immediately, rolls back on error

