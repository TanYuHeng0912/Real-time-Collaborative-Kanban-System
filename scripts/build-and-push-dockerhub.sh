#!/bin/bash
# Build and push Docker images to Docker Hub
# Usage: ./scripts/build-and-push-dockerhub.sh [version]

set -e

VERSION=${1:-latest}
DOCKER_USERNAME=${DOCKER_USERNAME:-tan0912}

# Docker Hub requires lowercase usernames - convert to lowercase
DOCKER_USERNAME=$(echo "$DOCKER_USERNAME" | tr '[:upper:]' '[:lower:]')

BACKEND_IMAGE="$DOCKER_USERNAME/kanban-backend"
FRONTEND_IMAGE="$DOCKER_USERNAME/kanban-frontend"

echo "Building and pushing Kanban System images to Docker Hub..."
echo "Version: $VERSION"
echo "Docker Username: $DOCKER_USERNAME"

# Build backend image
echo "Building backend image..."
docker build -t "$BACKEND_IMAGE:$VERSION" -t "$BACKEND_IMAGE:latest" -f Dockerfile .

# Build frontend image
echo "Building frontend image..."
docker build -t "$FRONTEND_IMAGE:$VERSION" -t "$FRONTEND_IMAGE:latest" \
  --build-arg VITE_API_BASE_URL=/api \
  --build-arg VITE_WS_URL=/api/ws \
  -f frontend/Dockerfile ./frontend

# Push backend image
echo "Pushing backend image..."
docker push "$BACKEND_IMAGE:$VERSION"
docker push "$BACKEND_IMAGE:latest"

# Push frontend image
echo "Pushing frontend image..."
docker push "$FRONTEND_IMAGE:$VERSION"
docker push "$FRONTEND_IMAGE:latest"

echo "Done! Images pushed to Docker Hub:"
echo "  - $BACKEND_IMAGE:$VERSION"
echo "  - $FRONTEND_IMAGE:$VERSION"

