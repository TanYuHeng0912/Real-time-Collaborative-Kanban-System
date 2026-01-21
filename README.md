# Real-Time Collaborative Kanban System

A full-stack Kanban board application built with Spring Boot and React, featuring real-time collaboration through WebSocket connections.

## 📚 Tech Stack

### Backend
- **Spring Boot 3.2+** with Java 17
- **Spring Data JPA** for database operations
- **Spring Security** with JWT authentication
- **PostgreSQL** for data persistence
- **Redis** for caching/session management (optional)
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

## 🚀 Installation Guide

Choose the installation method that best fits your needs:

| Method | Difficulty | Speed | Best For |
|--------|-----------|-------|----------|
| **Method 1: Docker Compose** | ⭐ Easy | ⚡ Fast | Beginners, Production |
| **Method 2: Docker Run Script** | ⭐ Easy | ⚡ Fast | Quick setup, No docker-compose |
| **Method 3: Manual Docker Run** | ⭐⭐ Medium | ⚡⚡ Medium | Full control, Custom config |
| **Method 4: Manual Build** | ⭐⭐⭐ Hard | ⚡⚡⚡ Slow | Developers, Debugging |

---

## Method 1: Docker Compose (Recommended ⭐)

**Best for**: Beginners, production deployments, managing complex configurations

### Prerequisites
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose (included with Docker Desktop)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/TanYuHeng0912/Real-time-Collaborative-Kanban-System.git
   cd Real-time-Collaborative-Kanban-System
   ```

2. **Create environment file** (`.env`)
   ```env
   # PostgreSQL Database
   POSTGRES_DB=kanban_db
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=your_secure_password_here
   
   # JWT Configuration (generate with: openssl rand -base64 64)
   JWT_SECRET=your_jwt_secret_key_min_64_characters_long_for_security
   JWT_EXPIRATION=86400000
   
   # Redis Configuration (optional)
   REDIS_PASSWORD=
   
   # Spring Configuration
   SPRING_JPA_DDL_AUTO=validate
   SPRING_JPA_SHOW_SQL=false
   SPRING_PROFILES_ACTIVE=prod
   
   # CORS Configuration
   CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
   WEBSOCKET_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
   
   # Docker Hub Configuration (optional - defaults to tan0912)
   DOCKER_USERNAME=tan0912
   ```

3. **Create Docker Compose file**
   ```bash
   cp docker-compose.dockerhub.yml.example docker-compose.dockerhub.yml
   ```

4. **Start all services**
   ```bash
   docker-compose -f docker-compose.dockerhub.yml up -d
   ```

5. **Wait for services to start** (30-60 seconds), then check status:
   ```bash
   docker-compose -f docker-compose.dockerhub.yml ps
   ```

6. **Initialize database** (first time only)
   
   **Windows PowerShell:**
   ```powershell
   Get-Content database/schema.sql | docker exec -i kanban-postgres psql -U postgres -d kanban_db
   Get-Content database/create_admin_render.sql | docker exec -i kanban-postgres psql -U postgres -d kanban_db
   ```
   
   **Linux/Mac:**
   ```bash
   docker exec -i kanban-postgres psql -U postgres -d kanban_db < database/schema.sql
   docker exec -i kanban-postgres psql -U postgres -d kanban_db < database/create_admin_render.sql
   ```

7. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8081/api
   - Admin Login: `admin@kanban.com` / `admin123`

---

## Method 2: Docker Run Script (No docker-compose needed!)

**Best for**: Quick setup, avoiding docker-compose files, using Docker Hub URLs directly

### Prerequisites
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Git (to clone repository for database scripts)

### Steps

1. **Clone the repository** (needed for database initialization scripts)
   ```bash
   git clone https://github.com/TanYuHeng0912/Real-time-Collaborative-Kanban-System.git
   cd Real-time-Collaborative-Kanban-System
   ```

2. **Set environment variables** (optional - defaults to `tan0912`)
   
   **Windows PowerShell:**
   ```powershell
   $env:DOCKER_HUB_USERNAME="tan0912"
   $env:POSTGRES_PASSWORD="your_secure_password"
   $env:JWT_SECRET="your_jwt_secret_key"
   ```
   
   **Linux/Mac:**
   ```bash
   export DOCKER_HUB_USERNAME=tan0912
   export POSTGRES_PASSWORD=your_secure_password
   export JWT_SECRET=your_jwt_secret_key
   ```

3. **Run the setup script**
   
   **Windows PowerShell:**
   ```powershell
   .\scripts\run-with-dockerhub-url.ps1
   ```
   
   **Linux/Mac:**
   ```bash
   chmod +x scripts/run-with-dockerhub-url.sh
   ./scripts/run-with-dockerhub-url.sh
   ```

4. **Initialize database** (same as Method 1, step 6)

5. **Access the application** (same as Method 1, step 7)

**What this does:**
- Automatically pulls images from Docker Hub: `tan0912/kanban-backend:latest` and `tan0912/kanban-frontend:latest`
- Creates Docker network
- Starts all containers (PostgreSQL, Redis, Backend, Frontend)
- No docker-compose file needed!

---

## Method 3: Manual Docker Run Commands

**Best for**: Maximum control, custom configurations, understanding how containers work

### Prerequisites
- Docker Desktop or Docker Engine

### Steps

```bash
# 1. Create network
docker network create kanban-network

# 2. Start PostgreSQL
docker run -d --name kanban-postgres --network kanban-network \
  -e POSTGRES_DB=kanban_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -v kanban_postgres_data:/var/lib/postgresql/data \
  --restart unless-stopped \
  postgres:16-alpine

# 3. Start Redis
docker run -d --name kanban-redis --network kanban-network \
  --restart unless-stopped \
  redis:7-alpine

# 4. Start Backend (from Docker Hub)
docker run -d --name kanban-backend --network kanban-network \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://kanban-postgres:5432/kanban_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_REDIS_HOST=kanban-redis \
  -e SPRING_REDIS_PORT=6379 \
  -e JWT_SECRET=your_jwt_secret \
  -e JWT_EXPIRATION=86400000 \
  -e SERVER_PORT=8080 \
  -e SERVER_SERVLET_CONTEXT_PATH=/api \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_JPA_DDL_AUTO=validate \
  -p 8081:8080 \
  --restart unless-stopped \
  tan0912/kanban-backend:latest

# 5. Start Frontend (from Docker Hub)
docker run -d --name kanban-frontend --network kanban-network \
  -p 3000:80 \
  --restart unless-stopped \
  tan0912/kanban-frontend:latest
```

**Then initialize database** (same as Method 1, step 6)

---

## Method 4: Manual Build & Run (For Developers)

**Best for**: Developers who want to modify code, debug, or understand the application structure

### Prerequisites
- **Java 17+**
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Node.js 18+** and **npm**
- **Redis** (optional, for caching)

### Backend Setup

1. **Database Setup**
   ```bash
   # Start PostgreSQL server
   createdb kanban_db
   psql -U postgres -d kanban_db -f database/schema.sql
   ```

2. **Configure environment variables**
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/kanban_db
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=your_password
   export JWT_SECRET=your-secret-key-change-in-production
   ```
   
   Or edit `src/main/resources/application.yml` directly.

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
   Backend starts on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure API URL** (optional - defaults to `/api`)
   ```bash
   echo "VITE_API_BASE_URL=http://localhost:8080/api" > .env
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```
   
   Frontend starts on `http://localhost:5173`

### Local Docker Build (Alternative)

If you want to build Docker images locally instead of using pre-built ones:

```bash
# Create .env file (same as Method 1, step 2)
docker-compose up -d --build
```

Then follow database initialization steps from Method 1.

---

## 🧪 Testing

### Backend Tests

Run backend tests:
```bash
mvn test
```

**Test Coverage:**
- ✅ **Controllers**: Authentication and Board operations (9 tests)
- ✅ **Services**: Card, List, and Permission services (46 tests)
- ✅ **Repositories**: Data access layer tests (13 tests)
- ✅ **Integration**: WebSocket connection test (1 test)
- ✅ **Application**: Spring Boot context loading (1 test)

**Total: 70 backend tests** ✓

### Frontend Tests

Run frontend tests:
```bash
cd frontend
npm install  # Install test dependencies
npm test     # Run tests in watch mode
```

**Test Coverage:**
- ✅ **Components**: React component rendering and interaction (4 tests)
- ✅ **Services**: Authentication service API calls (4 tests)

**Total: 8 frontend tests** ✓

**Test Frameworks:**
- Backend: JUnit 5, Mockito, Spring Boot Test
- Frontend: Vitest, React Testing Library

---

## ✨ Key Features

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

---

## 🔒 Security Features

- **Non-root Docker containers**: Application runs as `kanban` user
- **Secure base images**: Uses Alpine Linux variants
- **Environment variables**: All secrets externalized
- **Internal networking**: Database ports not exposed to host
- **Health checks**: Built-in health monitoring endpoints
- **CORS configuration**: Configurable allowed origins
- **JWT authentication**: Secure token-based authentication

