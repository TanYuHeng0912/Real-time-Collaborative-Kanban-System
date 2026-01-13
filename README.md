# High-Concurrency Collaborative Kanban System

A full-stack Kanban board application built with Spring Boot and React, featuring real-time collaboration through WebSocket connections.

## Tech Stack

### Backend
- **Spring Boot 3.2+** with Java 17
- **Spring Data JPA** for database operations
- **Spring Security** with JWT authentication
- **MySQL 8.0+** for data persistence
- **Redis** for caching/session management (configured but optional)
- **WebSocket (STOMP)** for real-time updates
- **Project Lombok** for reducing boilerplate code

### Frontend
- **React 18** with Functional Components
- **TypeScript** (Strict Mode)
- **Vite** as build tool
- **Tailwind CSS** for styling
- **Shadcn/UI** components
- **Zustand** for state management with optimistic updates
- **@tanstack/react-query** for server-state synchronization
- **@hello-pangea/dnd** for drag-and-drop functionality
- **Axios** with interceptors for API calls
- **WebSocket (STOMP)** client for real-time updates

## Project Structure

```
KANBAN SYSTEM/
├── database/
│   └── schema.sql              # Database schema
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── hooks/              # Custom React hooks
│   │   ├── lib/                # Utilities and API client
│   │   ├── pages/              # Page components
│   │   ├── services/           # API service layer
│   │   ├── store/              # Zustand stores
│   │   └── types/              # TypeScript types
│   └── package.json
├── src/
│   └── main/
│       ├── java/com/kanban/
│       │   ├── config/         # Configuration classes
│       │   ├── controller/     # REST controllers
│       │   ├── dto/            # Data Transfer Objects
│       │   ├── exception/      # Exception handlers
│       │   ├── model/          # JPA entities
│       │   ├── repository/     # JPA repositories
│       │   ├── security/       # Security configuration
│       │   ├── service/        # Business logic
│       │   └── util/           # Utility classes
│       └── resources/
│           └── application.yml # Application configuration
└── pom.xml                     # Maven dependencies

```

## Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Node.js 18+** and **npm**
- **Redis** (optional, for caching)

## Setup Instructions

### 1. Database Setup

1. Start MySQL server
2. Run the schema script:
   ```sql
   mysql -u root -p < database/schema.sql
   ```
   Or manually execute the SQL in `database/schema.sql`

### 2. Backend Setup

1. Update `src/main/resources/application.yml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/kanban_db
       username: your_username
       password: your_password
   ```

2. Update JWT secret in `application.yml` (use a secure random string in production)

3. Build and run the backend:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### 3. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173`

## Key Features

### Authentication
- JWT-based authentication
- User registration and login
- Protected routes
- Token stored in localStorage with automatic refresh handling

### Kanban Board
- Create and manage boards
- Create lists (columns) within boards
- Create, update, move, and delete cards
- Drag-and-drop cards between lists
- Real-time updates via WebSocket

### Real-time Collaboration
- WebSocket (STOMP) connection for real-time updates
- When User A moves a card, User B sees the movement instantly
- Optimistic UI updates with rollback on failure
- Board-specific WebSocket channels

### State Management
- **Zustand** for client-side state with optimistic updates
- **React Query** for server-state caching and synchronization
- Automatic error handling and retry logic

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login user

### Boards
- `GET /api/boards/{id}` - Get board by ID
- `GET /api/boards/workspace/{workspaceId}` - Get all boards in workspace
- `POST /api/boards` - Create a new board
- `PUT /api/boards/{id}` - Update board
- `DELETE /api/boards/{id}` - Delete board (soft delete)

### Lists
- `GET /api/lists/{id}` - Get list by ID
- `GET /api/lists/board/{boardId}` - Get all lists in a board
- `POST /api/lists` - Create a new list
- `PUT /api/lists/{id}` - Update list
- `DELETE /api/lists/{id}` - Delete list (soft delete)

### Cards
- `GET /api/cards/{id}` - Get card by ID
- `GET /api/cards/list/{listId}` - Get all cards in a list
- `POST /api/cards` - Create a new card
- `PUT /api/cards/{id}` - Update card
- `POST /api/cards/{id}/move` - Move card to another list
- `DELETE /api/cards/{id}` - Delete card (soft delete)

### WebSocket
- WebSocket endpoint: `/ws`
- Subscribe to board updates: `/topic/board/{boardId}`

## Architecture Highlights

### Backend Architecture
- **DTO Pattern**: All API responses use DTOs to avoid exposing internal entities
- **Soft Deletes**: All entities support soft deletion via `is_deleted` flag
- **Audit Fields**: `created_at` and `updated_at` automatically tracked
- **Global Exception Handler**: Centralized error handling with `@RestControllerAdvice`
- **JWT Security**: Stateless authentication with JWT tokens
- **WebSocket Broadcasting**: Real-time updates broadcasted to board subscribers

### Frontend Architecture
- **Optimistic Updates**: UI updates immediately, rolls back on error
- **Axios Interceptors**: Automatic JWT token injection and error handling
- **React Query**: Server-state caching, background refetching, and synchronization
- **Zustand Stores**: Lightweight state management for auth and kanban board state
- **TypeScript Strict Mode**: Type safety throughout the application
- **Component Composition**: Reusable UI components following Shadcn/UI patterns

## Development Notes

### Database Schema
- All tables include `is_deleted`, `created_at`, and `updated_at` fields
- Indexes on frequently queried columns (`board_id`, `user_id`, etc.)
- Foreign key constraints for data integrity

### Security
- JWT tokens expire after 24 hours (configurable)
- Password hashing using BCrypt
- CORS configured for frontend origins
- Security filter chain configured for public and protected endpoints

### WebSocket Implementation
- STOMP over SockJS for WebSocket communication
- Board-specific channels: `/topic/board/{boardId}`
- Card update messages include type (CREATED, UPDATED, MOVED, DELETED)
- Automatic reconnection on connection loss

## Testing

### Backend
```bash
mvn test
```

### Frontend
```bash
cd frontend
npm test
```

## Production Considerations

1. **Environment Variables**: Move sensitive configuration to environment variables
2. **JWT Secret**: Use a strong, randomly generated secret (256+ bits)
3. **HTTPS**: Enable HTTPS in production
4. **Database Connection Pooling**: Already configured with HikariCP
5. **Redis**: Enable Redis for session management and caching in production
6. **Error Logging**: Implement proper logging framework (Logback/SLF4J)
7. **Rate Limiting**: Consider implementing rate limiting for API endpoints
8. **WebSocket Authentication**: Add authentication to WebSocket connections

## License

This project is provided as-is for educational and development purposes.

