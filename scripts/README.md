# Maintainer Scripts

⚠️ **These scripts are for project maintainers only, not for end users.**

These scripts are used to build and push Docker images to Docker Hub. End users should use pre-built images from Docker Hub instead of building locally.

## Build and Push to Docker Hub

### Windows PowerShell
```powershell
.\scripts\build-and-push-dockerhub.ps1 [version]
```

### Linux/Mac
```bash
chmod +x scripts/build-and-push-dockerhub.sh
./scripts/build-and-push-dockerhub.sh [version]
```

**Prerequisites:**
1. Docker installed and running
2. Logged in to Docker Hub: `docker login`
3. Set `DOCKER_USERNAME` environment variable or modify script with your username

**Note:** Replace `[version]` with a version tag (e.g., `v1.0.0`) or omit for `latest`.

