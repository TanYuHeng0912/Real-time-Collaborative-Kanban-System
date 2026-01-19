#!/bin/bash
# Run Kanban System using Docker Hub images directly via URL
# No docker-compose file needed!
# Usage: ./scripts/run-with-dockerhub-url.sh

set -e

# Configuration (can be overridden via environment variables)
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-tan0912}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-changeme}
JWT_SECRET=${JWT_SECRET:-your-secret-key-change-in-production-min-256-bits-long}

NETWORK_NAME="kanban-network"
POSTGRES_CONTAINER="kanban-postgres"
REDIS_CONTAINER="kanban-redis"
BACKEND_CONTAINER="kanban-backend"
FRONTEND_CONTAINER="kanban-frontend"

echo "Starting Kanban System using Docker Hub images..."
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"

# Create network if not exists
echo "Creating Docker network..."
docker network create $NETWORK_NAME 2>/dev/null || true

# Start PostgreSQL
echo "Starting PostgreSQL..."
docker run -d \
  --name $POSTGRES_CONTAINER \
  --network $NETWORK_NAME \
  -e POSTGRES_DB=kanban_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  --restart unless-stopped \
  postgres:16-alpine

# Start Redis
echo "Starting Redis..."
docker run -d \
  --name $REDIS_CONTAINER \
  --network $NETWORK_NAME \
  --restart unless-stopped \
  redis:7-alpine

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5
until docker exec $POSTGRES_CONTAINER pg_isready -U postgres > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

# Start Backend
echo "Starting Backend (from Docker Hub: $DOCKER_HUB_USERNAME/kanban-backend:latest)..."
docker run -d \
  --name $BACKEND_CONTAINER \
  --network $NETWORK_NAME \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://$POSTGRES_CONTAINER:5432/kanban_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD \
  -e SPRING_REDIS_HOST=$REDIS_CONTAINER \
  -e SPRING_REDIS_PORT=6379 \
  -e JWT_SECRET=$JWT_SECRET \
  -e JWT_EXPIRATION=86400000 \
  -e SERVER_PORT=8080 \
  -e SERVER_SERVLET_CONTEXT_PATH=/api \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_JPA_DDL_AUTO=validate \
  -p 8081:8080 \
  --restart unless-stopped \
  $DOCKER_HUB_USERNAME/kanban-backend:latest

# Start Frontend
echo "Starting Frontend (from Docker Hub: $DOCKER_HUB_USERNAME/kanban-frontend:latest)..."
docker run -d \
  --name $FRONTEND_CONTAINER \
  --network $NETWORK_NAME \
  -p 3000:80 \
  --restart unless-stopped \
  $DOCKER_HUB_USERNAME/kanban-frontend:latest

echo ""
echo "✅ All containers started!"
echo ""
echo "Next steps:"
echo "1. Initialize database: docker exec -i $POSTGRES_CONTAINER psql -U postgres -d kanban_db < database/schema.sql"
echo "2. Create admin user: docker exec -i $POSTGRES_CONTAINER psql -U postgres -d kanban_db < database/create_admin_render.sql"
echo "3. Access: http://localhost:3000"
echo ""
echo "To stop all containers:"
echo "  docker stop $FRONTEND_CONTAINER $BACKEND_CONTAINER $REDIS_CONTAINER $POSTGRES_CONTAINER"
echo "  docker rm $FRONTEND_CONTAINER $BACKEND_CONTAINER $REDIS_CONTAINER $POSTGRES_CONTAINER"

