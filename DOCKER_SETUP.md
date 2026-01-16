# Docker Setup Guide

This guide will help you set up and run the Kanban System using Docker Compose.

## Prerequisites

- Docker Desktop installed and running (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v3.8 or higher

## Step 1: Create Environment File

Create a `.env` file in the project root directory:

```env
# PostgreSQL Database Configuration
POSTGRES_DB=kanban_db
POSTGRES_USER=postgres
# SECURITY: Use a strong password in production!
# Generate: openssl rand -base64 32
POSTGRES_PASSWORD=changeme_use_strong_password

# Redis Configuration (Optional - can leave empty if not using Redis)
REDIS_PASSWORD=

# JWT Authentication
# SECURITY: Generate a secure secret: openssl rand -base64 64
# Must be at least 256 bits (32 bytes)
JWT_SECRET=your-secret-key-change-in-production-min-256-bits-long-generate-with-openssl
JWT_EXPIRATION=86400000

# Spring Boot Configuration
SPRING_JPA_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_PROFILES_ACTIVE=prod

# CORS Configuration (for backend)
# Comma-separated list of allowed origins
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# WebSocket Configuration (for backend)
# Comma-separated list of allowed origins
WEBSOCKET_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

**Important:** Replace the placeholder values with secure ones:
- Generate JWT_SECRET: `openssl rand -base64 64`
- Use a strong POSTGRES_PASSWORD

## Step 2: Build and Start Services

```powershell
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f backend
docker-compose logs -f frontend
```

## Step 3: Initialize Database

After the services are running, you need to create the database schema:

```powershell
# Connect to PostgreSQL container
docker exec -it kanban-postgres psql -U postgres -d kanban_db

# Then copy and paste the contents of database/schema.sql
# Or use:
docker exec -i kanban-postgres psql -U postgres -d kanban_db < database/schema.sql
```

## Step 4: Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/health

## Step 5: Create Admin User (Optional)

After the database is initialized, create an admin user:

```powershell
# Connect to PostgreSQL
docker exec -it kanban-postgres psql -U postgres -d kanban_db

# Run the admin creation script
# Copy contents of database/create_admin_render.sql and paste, or:
docker exec -i kanban-postgres psql -U postgres -d kanban_db < database/create_admin_render.sql
```

Default admin credentials (change in production!):
- Username: `admin`
- Password: `admin123`

## Common Commands

```powershell
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v

# Rebuild services
docker-compose up -d --build

# View running containers
docker-compose ps

# Execute command in container
docker exec -it kanban-backend sh
docker exec -it kanban-postgres psql -U postgres -d kanban_db

# View container logs
docker logs kanban-backend
docker logs kanban-frontend
docker logs kanban-postgres
```

## Troubleshooting

### Services won't start
- Check if ports 3000 and 8080 are already in use
- Verify Docker Desktop is running
- Check logs: `docker-compose logs`

### Database connection errors
- Wait for PostgreSQL to fully start (health check takes ~10-30 seconds)
- Verify POSTGRES_PASSWORD in `.env` matches docker-compose.yml
- Check backend logs: `docker-compose logs backend`

### Frontend can't connect to backend
- Verify backend is running: `curl http://localhost:8080/api/health`
- Check CORS_ALLOWED_ORIGINS includes `http://localhost:3000`
- Check backend logs for CORS errors

### Can't access frontend
- Verify frontend container is running: `docker-compose ps`
- Check frontend logs: `docker-compose logs frontend`
- Try accessing directly: http://localhost:3000

## Security Notes

- ✅ Database port (5432) is **NOT exposed** to host - only accessible within Docker network
- ✅ Redis port is **NOT exposed** to host
- ✅ Containers run as non-root users
- ✅ All secrets use environment variables
- ⚠️ **NEVER commit `.env` file to Git!**
- ⚠️ Change default passwords before production use

## Stopping the Application

```powershell
# Stop all services (data persists)
docker-compose stop

# Stop and remove containers (data persists)
docker-compose down

# Stop and remove everything including volumes (WARNING: deletes data)
docker-compose down -v
```

