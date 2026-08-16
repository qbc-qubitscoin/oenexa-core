Write-Host "Starting OENEXA Core Project locally..." -ForegroundColor Cyan

$rootDir = $PSScriptRoot
if (-not $rootDir) { $rootDir = (Get-Location).Path }
$uiDir = Join-Path (Split-Path $rootDir -Parent) "oenexa-ui"
if (-not (Test-Path $uiDir)) { $uiDir = "c:\workspace\oenexa-ui" }

# Start Docker Compose (MySQL, Kafka, Redis, etc.)
Write-Host "Starting Docker containers..." -ForegroundColor Green
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    docker-compose up -d
}

Write-Host "Waiting 5 seconds for databases and Kafka to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 5

# Array to store job objects
$jobs = @()

Write-Host "Starting Java Wallet Service..." -ForegroundColor Yellow
$jobs += Start-Job -ScriptBlock {
    param($dir)
    Set-Location -Path $dir
    .\gradlew :oenexa-wallet-service:bootRun
} -ArgumentList $rootDir

Write-Host "Starting Go Trading Service (with WebSocket Hub)..." -ForegroundColor Yellow
$jobs += Start-Job -ScriptBlock {
    param($dir)
    Set-Location -Path (Join-Path $dir "oenexa-trading-service")
    go run main.go
} -ArgumentList $rootDir

Write-Host "Starting Go Matching Engine..." -ForegroundColor Yellow
$jobs += Start-Job -ScriptBlock {
    param($dir)
    Set-Location -Path (Join-Path $dir "oenexa-matching-engine")
    go run main.go
} -ArgumentList $rootDir

Write-Host "Starting React UI..." -ForegroundColor Yellow
$jobs += Start-Job -ScriptBlock {
    param($dir)
    if (Test-Path $dir) {
        Set-Location -Path $dir
        npm run dev
    } else {
        Write-Warning "UI directory not found at $dir"
    }
} -ArgumentList $uiDir

Write-Host "All services started in background jobs!" -ForegroundColor Cyan
Write-Host "To view logs, use: Receive-Job -Id <JobId>" -ForegroundColor Gray
Write-Host "To stop everything, press Ctrl+C and run: Stop-Job -State Running" -ForegroundColor Gray

# Wait indefinitely so the console stays open, or until Ctrl+C
try {
    Wait-Job -State Running
}
catch {
    Write-Host "Stopping all services..." -ForegroundColor Red
    $jobs | Stop-Job
}
