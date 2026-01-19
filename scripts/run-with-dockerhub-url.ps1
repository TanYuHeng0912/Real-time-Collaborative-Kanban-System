# Run Kanban System using Docker Hub images directly via URL
# No docker-compose file needed!
# Usage: .\scripts\run-with-dockerhub-url.ps1

param(
    [string]$DockerHubUsername = $env:DOCKER_HUB_USERNAME,
    [string]$PostgresPassword = $env:POSTGRES_PASSWORD,
    [string]$JwtSecret = $env:JWT_SECRET
)

if (-not $DockerHubUsername) {
    $DockerHubUsername = "tan0912"
}

if (-not $PostgresPassword) {
    $PostgresPassword = "changeme"
    Write-Warning "Using default POSTGRES_PASSWORD. Please set it for production!"
}

if (-not $JwtSecret) {
    $JwtSecret = "your-secret-key-change-in-production-min-256-bits-long"
    Write-Warning "Using default JWT_SECRET. Please set it for production!"
}

$NetworkName = "kanban-network"
$PostgresContainer = "kanban-postgres"
$RedisContainer = "kanban-redis"
$BackendContainer = "kanban-backend"
$FrontendContainer = "kanban-frontend"

Write-Host "Starting Kanban System using Docker Hub images..." -ForegroundColor Cyan
Write-Host "Docker Hub Username: $DockerHubUsername"

# Create network if not exists
Write-Host "`nCreating Docker network..." -ForegroundColor Yellow
docker network create $NetworkName 2>$null

# Start PostgreSQL
Write-Host "Starting PostgreSQL..." -ForegroundColor Yellow
docker run -d `
  --name $PostgresContainer `
  --network $NetworkName `
  -e POSTGRES_DB=kanban_db `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=$PostgresPassword `
  --restart unless-stopped `
  postgres:16-alpine

# Start Redis
Write-Host "Starting Redis..." -ForegroundColor Yellow
docker run -d `
  --name $RedisContainer `
  --network $NetworkName `
  --restart unless-stopped `
  redis:7-alpine

# Wait for PostgreSQL to be ready
Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
$maxAttempts = 30
$attempt = 0
while ($attempt -lt $maxAttempts) {
    $result = docker exec $PostgresContainer pg_isready -U postgres 2>&1
    if ($LASTEXITCODE -eq 0) {
        break
    }
    Start-Sleep -Seconds 2
    $attempt++
}

# Start Backend
$BackendImage = "$DockerHubUsername/kanban-backend:latest"
Write-Host "Starting Backend (from Docker Hub: $BackendImage)..." -ForegroundColor Yellow
docker run -d `
  --name $BackendContainer `
  --network $NetworkName `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://$PostgresContainer`:5432/kanban_db" `
  -e SPRING_DATASOURCE_USERNAME=postgres `
  -e SPRING_DATASOURCE_PASSWORD=$PostgresPassword `
  -e SPRING_REDIS_HOST=$RedisContainer `
  -e SPRING_REDIS_PORT=6379 `
  -e JWT_SECRET=$JwtSecret `
  -e JWT_EXPIRATION=86400000 `
  -e SERVER_PORT=8080 `
  -e SERVER_SERVLET_CONTEXT_PATH=/api `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e SPRING_JPA_DDL_AUTO=validate `
  -p 8081:8080 `
  --restart unless-stopped `
  $BackendImage

# Start Frontend
$FrontendImage = "$DockerHubUsername/kanban-frontend:latest"
Write-Host "Starting Frontend (from Docker Hub: $FrontendImage)..." -ForegroundColor Yellow
docker run -d `
  --name $FrontendContainer `
  --network $NetworkName `
  -p 3000:80 `
  --restart unless-stopped `
  $FrontendImage

Write-Host "`n✅ All containers started!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Initialize database:" -ForegroundColor White
Write-Host "   Get-Content database/schema.sql | docker exec -i $PostgresContainer psql -U postgres -d kanban_db" -ForegroundColor Gray
Write-Host "2. Create admin user:" -ForegroundColor White
Write-Host "   Get-Content database/create_admin_render.sql | docker exec -i $PostgresContainer psql -U postgres -d kanban_db" -ForegroundColor Gray
Write-Host "3. Access: http://localhost:3000" -ForegroundColor White
Write-Host ""
Write-Host "To stop all containers:" -ForegroundColor Cyan
Write-Host "   docker stop $FrontendContainer $BackendContainer $RedisContainer $PostgresContainer" -ForegroundColor Gray
Write-Host "   docker rm $FrontendContainer $BackendContainer $RedisContainer $PostgresContainer" -ForegroundColor Gray

