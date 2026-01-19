# Build and push Docker images to Docker Hub
# Usage: .\scripts\build-and-push-dockerhub.ps1 [version]

param(
    [string]$Version = "latest",
    [string]$DockerUsername = $env:DOCKER_USERNAME
)

if (-not $DockerUsername) {
    $DockerUsername = "tan0912"
    Write-Host "Using default Docker Hub username: $DockerUsername" -ForegroundColor Yellow
}

# Docker Hub requires lowercase usernames - convert to lowercase
$DockerUsername = $DockerUsername.ToLower()

$BackendImage = "$DockerUsername/kanban-backend"
$FrontendImage = "$DockerUsername/kanban-frontend"

Write-Host "Building and pushing Kanban System images to Docker Hub..." -ForegroundColor Cyan
Write-Host "Version: $Version"
Write-Host "Docker Username: $DockerUsername"

# Build backend image
Write-Host "`nBuilding backend image..." -ForegroundColor Yellow
docker build -t "$BackendImage`:$Version" -t "$BackendImage`:latest" -f Dockerfile .

if ($LASTEXITCODE -ne 0) {
    Write-Error "Backend build failed"
    exit 1
}

# Build frontend image
Write-Host "`nBuilding frontend image..." -ForegroundColor Yellow
docker build -t "$FrontendImage`:$Version" -t "$FrontendImage`:latest" `
    --build-arg VITE_API_BASE_URL=/api `
    --build-arg VITE_WS_URL=/api/ws `
    -f frontend/Dockerfile ./frontend

if ($LASTEXITCODE -ne 0) {
    Write-Error "Frontend build failed"
    exit 1
}

# Push backend image
Write-Host "`nPushing backend image..." -ForegroundColor Yellow
docker push "$BackendImage`:$Version"
docker push "$BackendImage`:latest"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Backend push failed. Make sure you're logged in to Docker Hub (docker login)"
    exit 1
}

# Push frontend image
Write-Host "`nPushing frontend image..." -ForegroundColor Yellow
docker push "$FrontendImage`:$Version"
docker push "$FrontendImage`:latest"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Frontend push failed. Make sure you're logged in to Docker Hub (docker login)"
    exit 1
}

Write-Host "`nDone! Images pushed to Docker Hub:" -ForegroundColor Green
Write-Host "  - $BackendImage`:$Version"
Write-Host "  - $FrontendImage`:$Version"

