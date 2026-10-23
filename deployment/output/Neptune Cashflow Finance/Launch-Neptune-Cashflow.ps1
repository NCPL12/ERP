[CmdletBinding()]
param([switch]$Activate, [switch]$ResetLogin)

$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Split-Path -Parent $MyInvocation.MyCommand.Path)).TrimEnd('\')
$registrationRoot = Join-Path $env:LOCALAPPDATA 'NeptuneCashflow'
$activeInstallFile = Join-Path $registrationRoot 'active-install.txt'
$appUrl = 'http://127.0.0.1:8081/cashflow-analyzer/overview'
$healthUrl = 'http://127.0.0.1:8081/login'
$javaExe = Join-Path $root 'runtime\bin\javaw.exe'
$appWar = Join-Path $root 'app\neptune-cashflow.war'
$setupMarker = Join-Path $root 'data\.setup-complete'
$logFile = Join-Path $root 'logs\application.log'

function Get-CashflowProcesses {
    return @(Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object {
        $_.Name -match '^javaw?\.exe$' -and ($_.CommandLine -like '*neptune-cashflow.war*' -or $_.CommandLine -like '*cashflow-standalone*')
    })
}

function Test-ThisInstallationRunning {
    $escapedWar = [Regex]::Escape($appWar)
    return @((Get-CashflowProcesses) | Where-Object { $_.CommandLine -match $escapedWar }).Count -gt 0
}

function Test-AppReady {
    try { $response = Invoke-WebRequest -UseBasicParsing -Uri $healthUrl -TimeoutSec 2; return $response.StatusCode -eq 200 } catch { return $false }
}

function Open-CashflowApp {
    $edgeCandidates = @((Join-Path ${env:ProgramFiles(x86)} 'Microsoft\Edge\Application\msedge.exe'), (Join-Path $env:ProgramFiles 'Microsoft\Edge\Application\msedge.exe'))
    $chromeCandidates = @((Join-Path $env:ProgramFiles 'Google\Chrome\Application\chrome.exe'), (Join-Path ${env:ProgramFiles(x86)} 'Google\Chrome\Application\chrome.exe'))
    $browser = @($edgeCandidates + $chromeCandidates | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -First 1)
    if ($browser.Count -gt 0) { Start-Process -FilePath $browser[0] -ArgumentList "--app=$appUrl", '--start-maximized' } else { Start-Process $appUrl }
}

if (-not $Activate -and (Test-Path -LiteralPath $activeInstallFile)) {
    $activeRoot = (Get-Content -LiteralPath $activeInstallFile -Raw).Trim()
    if ($activeRoot) { try { $activeRoot = [IO.Path]::GetFullPath($activeRoot).TrimEnd('\') } catch { $activeRoot = $null } }
    if ($activeRoot -and -not [string]::Equals($activeRoot, $root, [StringComparison]::OrdinalIgnoreCase)) {
        $activeStart = Join-Path $activeRoot 'Start Neptune Cashflow.cmd'
        if (Test-Path -LiteralPath $activeStart) {
            Write-Host "Opening the active Neptune Cashflow installation: $activeRoot" -ForegroundColor Cyan
            if ($ResetLogin) {
                & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $activeRoot 'Launch-Neptune-Cashflow.ps1') -ResetLogin
            } else {
                Start-Process -FilePath $activeStart -WorkingDirectory $activeRoot
            }
            exit 0
        }
    }
}

New-Item -ItemType Directory -Path $registrationRoot -Force | Out-Null
Set-Content -LiteralPath $activeInstallFile -Value $root -Encoding UTF8
if (-not (Test-Path -LiteralPath $javaExe)) { throw 'Bundled Java runtime is missing. Re-extract the full package.' }
if (-not (Test-Path -LiteralPath $appWar)) { throw 'Application file is missing. Re-extract the full package.' }
@('data','logs','uploads') | ForEach-Object { New-Item -ItemType Directory -Path (Join-Path $root $_) -Force | Out-Null }

if ($ResetLogin) {
    Write-Host ''
    Write-Host 'NEPTUNE CASHFLOW - RESET ADMINISTRATOR LOGIN' -ForegroundColor Cyan
    Write-Host 'Stopping the current Cashflow application...' -ForegroundColor Yellow
    $runningProcesses = @(Get-CashflowProcesses)
    foreach ($process in $runningProcesses) {
        Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
        Wait-Process -Id $process.ProcessId -Timeout 15 -ErrorAction SilentlyContinue
    }
    $dataRoot = [IO.Path]::GetFullPath((Join-Path $root 'data')).TrimEnd('\')
    if (-not $dataRoot.StartsWith($root + '\', [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Refusing to reset because the data path is outside the active installation.'
    }
    Get-ChildItem -LiteralPath $dataRoot -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force
    Write-Host 'Old local login cleared. Create the administrator credentials below.' -ForegroundColor Green
}

if ((Test-ThisInstallationRunning) -and (Test-AppReady)) { Open-CashflowApp; exit 0 }
$otherProcesses = @(Get-CashflowProcesses)
foreach ($process in $otherProcesses) {
    Write-Host "Stopping another Neptune Cashflow instance ($($process.ProcessId))..." -ForegroundColor Yellow
    Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
}
if ($otherProcesses.Count -gt 0) { Start-Sleep -Seconds 2 }
$foreignListener = Get-NetTCPConnection -State Listen -LocalPort 8081 -ErrorAction SilentlyContinue
if ($foreignListener) { throw 'Port 8081 is being used by another application. Close it and start Neptune Cashflow again.' }

$firstRun = -not (Test-Path -LiteralPath $setupMarker)
if ($firstRun) {
    Write-Host ''
    Write-Host 'NEPTUNE CASHFLOW - FIRST-TIME SETUP' -ForegroundColor Cyan
    Write-Host 'The finance database is embedded and will be created automatically.'
    Write-Host 'No SQL or MySQL installation/password is required.' -ForegroundColor Green
    Write-Host ''
    do { $adminUsername = (Read-Host 'Create administrator username').Trim() } while ([string]::IsNullOrWhiteSpace($adminUsername))
    do {
        $passwordOne = Read-Host 'Create administrator password (minimum 8 characters)' -AsSecureString
        $passwordTwo = Read-Host 'Confirm administrator password' -AsSecureString
        $bstrOne = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($passwordOne)
        $bstrTwo = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($passwordTwo)
        try { $plainOne = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstrOne); $plainTwo = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstrTwo) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstrOne); [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstrTwo) }
        if ($plainOne.Length -lt 8) { Write-Host 'Password must contain at least 8 characters.' -ForegroundColor Yellow; $validPassword = $false }
        elseif ($plainOne -cne $plainTwo) { Write-Host 'Passwords did not match. Try again.' -ForegroundColor Yellow; $validPassword = $false }
        else { $validPassword = $true }
    } while (-not $validPassword)
    $env:CASHFLOW_BOOTSTRAP_ADMIN_USERNAME = $adminUsername
    $env:CASHFLOW_BOOTSTRAP_ADMIN_PASSWORD = $plainOne
}

Write-Host 'Starting Neptune Cashflow Analyzer...' -ForegroundColor Cyan
$quotedAppWar = '"' + $appWar + '"'
$arguments = @('-Dfile.encoding=UTF-8', '-jar', $quotedAppWar, '--spring.profiles.active=dev,cashflow-test,cashflow-standalone', '--cashflow.tally.server.url=http://192.168.0.183:9000', '--cashflow.tally.company-name=Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)', '--cashflow.tally.sync.enabled=true', '--cashflow.tally.sync.initial-delay-ms=5000', '--cashflow.tally.sync.interval-ms=60000', '--logging.file.name=./logs/application.log')
$server = Start-Process -FilePath $javaExe -ArgumentList $arguments -WorkingDirectory $root -WindowStyle Hidden -PassThru
Remove-Item Env:CASHFLOW_BOOTSTRAP_ADMIN_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:CASHFLOW_BOOTSTRAP_ADMIN_USERNAME -ErrorAction SilentlyContinue
$plainOne = $null; $plainTwo = $null

for ($attempt = 1; $attempt -le 180; $attempt++) {
    if ($server.HasExited) { break }
    if (Test-AppReady) {
        if ($firstRun) { Set-Content -LiteralPath $setupMarker -Value (Get-Date -Format o) -Encoding ASCII }
        Write-Host 'Ready. Opening login...' -ForegroundColor Green
        Open-CashflowApp
        exit 0
    }
    Start-Sleep -Seconds 1
}

Write-Host ''
Write-Host 'Neptune Cashflow did not start.' -ForegroundColor Red
Write-Host "Review the log: $logFile"
if (Test-Path -LiteralPath $logFile) { Get-Content -LiteralPath $logFile -Tail 25 }
Read-Host 'Press Enter to close'
exit 1
