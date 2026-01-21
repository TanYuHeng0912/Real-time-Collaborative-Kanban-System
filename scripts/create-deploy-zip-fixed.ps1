# Create deployment ZIP with Unix-style paths (forward slashes only)
# This script manually creates ZIP entries with correct path format

param(
    [string]$SourceFolder = "frontend/dist",
    [string]$OutputFile = "frontend/deploy.zip"
)

Write-Host "Creating deployment ZIP with Unix-style paths..." -ForegroundColor Green

# Check if source folder exists
if (-not (Test-Path $SourceFolder)) {
    Write-Host "❌ Source folder not found: $SourceFolder" -ForegroundColor Red
    exit 1
}

# Remove existing ZIP if it exists
if (Test-Path $OutputFile) {
    Write-Host "Removing existing ZIP file..." -ForegroundColor Yellow
    Remove-Item $OutputFile -Force
}

# Load required assemblies
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$sourcePath = (Resolve-Path $SourceFolder).Path
$outputPath = Join-Path (Get-Location) $OutputFile

Write-Host "Source: $sourcePath" -ForegroundColor Gray
Write-Host "Output: $outputPath" -ForegroundColor Gray

try {
    # Create ZIP file
    $zipFile = [System.IO.Compression.ZipFile]::Open($outputPath, [System.IO.Compression.ZipArchiveMode]::Create)
    
    # Get all files recursively
    $files = Get-ChildItem -Path $sourcePath -Recurse -File
    
    Write-Host "`nAdding files to ZIP..." -ForegroundColor Yellow
    
    foreach ($file in $files) {
        # Get relative path from source folder
        # Use string manipulation to get relative path
        $fileFullPath = $file.FullName
        $relativePath = $fileFullPath.Substring($sourcePath.Length + 1)
        
        # Convert Windows path separators to Unix-style (forward slashes)
        $zipEntryName = $relativePath.Replace('\', '/')
        
        # Create entry with Unix-style path
        $entry = $zipFile.CreateEntry($zipEntryName)
        
        # Copy file content
        $entryStream = $entry.Open()
        $fileStream = [System.IO.File]::OpenRead($file.FullName)
        $fileStream.CopyTo($entryStream)
        $fileStream.Close()
        $entryStream.Close()
        
        Write-Host "  Added: $zipEntryName" -ForegroundColor Gray
    }
    
    $zipFile.Dispose()
    
    $zipSize = (Get-Item $outputPath).Length / 1MB
    Write-Host "`n✅ ZIP file created successfully!" -ForegroundColor Green
    Write-Host "   Size: $([math]::Round($zipSize, 2)) MB" -ForegroundColor Cyan
    Write-Host "   Location: $outputPath" -ForegroundColor Cyan
    
    # Verify ZIP contents
    Write-Host "`nVerifying ZIP contents..." -ForegroundColor Yellow
    $verifyZip = [System.IO.Compression.ZipFile]::OpenRead($outputPath)
    Write-Host "`nFiles in ZIP (should use forward slashes):" -ForegroundColor Cyan
    foreach ($entry in $verifyZip.Entries) {
        Write-Host "  $($entry.FullName)" -ForegroundColor Gray
        if ($entry.FullName -match '\\') {
            Write-Host "    ⚠️  WARNING: Contains backslash!" -ForegroundColor Red
        }
    }
    $verifyZip.Dispose()
    
} catch {
    Write-Host "`n❌ Error creating ZIP: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Deployment ZIP ready for upload!" -ForegroundColor Green

