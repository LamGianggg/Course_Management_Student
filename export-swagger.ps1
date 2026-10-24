# Script tu dong xuat file OpenAPI / Swagger JSON tu he thong
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Doc version tu pom.xml
[xml]$pom = Get-Content (Join-Path $scriptDir "pom.xml")
$version = $pom.project.version
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " [OpenAPI Export] Project Version: $version" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

$docsDir = Join-Path $scriptDir "docs"
if (-not (Test-Path $docsDir)) {
    New-Item -ItemType Directory -Path $docsDir | Out-Null
}

$outputFile = Join-Path $docsDir "openapi.json"
$apiUrl = "http://localhost:8080/v3/api-docs"

$isListening = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
$startedProcess = $null

if (-not $isListening) {
    Write-Host "[INFO] Backend chua chay. Dang khoi dong ung dung tam thoi de xuat Swagger..." -ForegroundColor Yellow
    $startedProcess = Start-Process mvn -ArgumentList "spring-boot:run" -PassThru -NoNewWindow
    
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 2
        try {
            $resp = Invoke-WebRequest -Uri $apiUrl -TimeoutSec 3 -ErrorAction SilentlyContinue
            if ($resp.StatusCode -eq 200) {
                $ready = $true
                break
            }
        } catch {}
    }

    if (-not $ready) {
        if ($startedProcess) { Stop-Process -Id $startedProcess.Id -Force -ErrorAction SilentlyContinue }
        Write-Error "[ERROR] Khong the khoi dong backend sau 60s de xuat Swagger."
    }
}

try {
    Write-Host "[INFO] Dang tai file dac ta OpenAPI tu $apiUrl..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $apiUrl -OutFile $outputFile
    Write-Host "[SUCCESS] Da xuat thanh cong file: $outputFile" -ForegroundColor Green
    Write-Host "[INFO] File nay da san sang de commit va push len GitHub!" -ForegroundColor Yellow
} finally {
    if ($startedProcess) {
        Write-Host "[INFO] Dang dung tien trinh tam thoi..." -ForegroundColor Cyan
        Stop-Process -Id $startedProcess.Id -Force -ErrorAction SilentlyContinue
    }
}
