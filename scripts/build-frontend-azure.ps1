# Build Frontend for Azure Deployment
# This script builds the frontend with Azure backend API URLs

param(
    [string]$BackendUrl = "https://kanban-backend-f5epg6agave6hme2.southeastasia-01.azurewebsites.net"
)

Write-Host "Building frontend for Azure deployment..." -ForegroundColor Green
Write-Host "Backend URL: $BackendUrl" -ForegroundColor Cyan

# Set environment variables for Vite build
$env:VITE_API_BASE_URL = "$BackendUrl/api"
$env:VITE_WS_URL = "$BackendUrl/api/ws"

Write-Host "`nEnvironment variables set:" -ForegroundColor Yellow
Write-Host "  VITE_API_BASE_URL=$env:VITE_API_BASE_URL"
Write-Host "  VITE_WS_URL=$env:VITE_WS_URL"

# Navigate to frontend directory
Push-Location frontend

try {
    # Install dependencies if needed
    if (-not (Test-Path "node_modules")) {
        Write-Host "`nInstalling dependencies..." -ForegroundColor Yellow
        npm install
    }

    # Build the application
    Write-Host "`nBuilding frontend application..." -ForegroundColor Yellow
    npm run build

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ Frontend build completed successfully!" -ForegroundColor Green
        Write-Host "`nBuild output: frontend/dist" -ForegroundColor Cyan
        Write-Host "`nNext steps:" -ForegroundColor Yellow
        Write-Host "  1. Deploy the 'frontend/dist' folder to Azure Static Web Apps"
        Write-Host "  2. Or use: az staticwebapp deploy --name <app-name> --source frontend/dist"
    } else {
        Write-Host "`n❌ Build failed!" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "`n❌ Error during build: $_" -ForegroundColor Red
    exit 1
} finally {
    Pop-Location
}





