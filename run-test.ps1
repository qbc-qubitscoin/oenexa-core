Write-Host "========================================="
Write-Host "Running BDD/TDD Tests & JaCoCo Coverage for OENEXA Core..." -ForegroundColor Cyan
Write-Host "========================================="

# 1. Java Services BDD/TDD & JaCoCo 100% Verification
Write-Host "Running Java Multi-Module Test Suites & JaCoCo Coverage Verification..." -ForegroundColor Yellow
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Java test or coverage verification failed." -ForegroundColor Red
    exit 1
}

# 2. Go Matching Engine
Write-Host "Running Go Matching Engine BDD/TDD Tests..." -ForegroundColor Yellow
Push-Location oenexa-matching-engine
go test -v -cover ./...
$goMatchExit = $LASTEXITCODE
Pop-Location
if ($goMatchExit -ne 0) {
    Write-Host "❌ Go Matching Engine tests failed." -ForegroundColor Red
    exit 1
}

# 3. Go Trading Service
Write-Host "Running Go Trading Service BDD/TDD Tests..." -ForegroundColor Yellow
Push-Location oenexa-trading-service
go test -v -cover ./...
$goTradeExit = $LASTEXITCODE
Pop-Location
if ($goTradeExit -ne 0) {
    Write-Host "❌ Go Trading Service tests failed." -ForegroundColor Red
    exit 1
}

Write-Host "========================================="
Write-Host "✅ All BDD/TDD tests & JaCoCo coverage verified successfully!" -ForegroundColor Green
Write-Host "========================================="
