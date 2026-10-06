<#
    Web Based Tour Guide - one-time automatic setup
    Group 2026-Y2-S1-MLB-WEB3G1-03

    Run SETUP.bat (which calls this) instead of running this file directly.

    What it does, so you can explain it if you are asked:
      1. finds your SQL Server instance from the Windows registry
      2. creates TourGuideDB and runs 01_schema.sql and 02_seed.sql
      3. creates a dedicated SQL login for the application (not 'sa')
      4. enables TCP/IP on port 1433 and mixed-mode authentication
      5. restarts the SQL Server service
      6. writes backend\.env for you
      7. builds the Java backend (Maven wrapper) and runs npm install in frontend
#>

$ErrorActionPreference = 'Stop'

# ---------------------------------------------------------------- helpers ---
function Say  ($m) { Write-Host "  $m" }
function Ok   ($m) { Write-Host "  [ OK ] $m"   -ForegroundColor Green }
function Warn ($m) { Write-Host "  [WARN] $m"   -ForegroundColor Yellow }
function Die  ($m) {
    Write-Host ""
    Write-Host "  [STOP] $m" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Nothing was broken - fix the above and run SETUP.bat again."
    Write-Host ""
    Read-Host "  Press Enter to close"
    exit 1
}
function Step ($n, $m) {
    Write-Host ""
    Write-Host "  [$n/7] $m" -ForegroundColor Cyan
}

# Reads a persisted (registry) environment variable directly, since a process
# launched elevated via 'Start-Process -Verb RunAs' inherits Explorer's cached
# environment block, which can be stale if a JDK was installed since Explorer
# last started - $env:JAVA_HOME / $env:Path alone are not reliable here.
function Get-PersistedEnvVar ($name) {
    foreach ($path in @('HKCU:\Environment', 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Environment')) {
        try {
            $v = (Get-ItemProperty -Path $path -Name $name -ErrorAction Stop).$name
            if ($v) { return $v }
        } catch { }
    }
    return $null
}

# Finds a JDK 21+ 'java' executable: JAVA_HOME first (session, then registry),
# then PATH (session, then registry), then the common install locations
# (Eclipse Adoptium / Oracle / Microsoft) as a fallback.
function Find-Java21 {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
    $persistedJavaHome = Get-PersistedEnvVar 'JAVA_HOME'
    if ($persistedJavaHome) { $candidates += (Join-Path $persistedJavaHome 'bin\java.exe') }

    $onPath = Get-Command java -ErrorAction SilentlyContinue
    if ($onPath) { $candidates += $onPath.Source }

    $persistedPath = Get-PersistedEnvVar 'Path'
    if ($persistedPath) {
        foreach ($dir in ($persistedPath -split ';')) {
            if ($dir) {
                $exe = Join-Path $dir 'java.exe'
                if (Test-Path $exe) { $candidates += $exe }
            }
        }
    }

    foreach ($base in @("$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Java", "$env:ProgramFiles\Microsoft")) {
        if (Test-Path $base) {
            Get-ChildItem $base -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                $exe = Join-Path $_.FullName 'bin\java.exe'
                if (Test-Path $exe) { $candidates += $exe }
            }
        }
    }
    foreach ($exe in ($candidates | Select-Object -Unique)) {
        # java -version writes to stderr; under $ErrorActionPreference = 'Stop',
        # PowerShell 5.1 turns a redirected native-command stderr line into a
        # terminating error, so this call must run with 'Continue' locally or
        # every candidate silently "fails" even when it is a valid JDK 21+.
        $prevPref = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            $verLine = & $exe -version 2>&1 | Select-Object -First 1
            if ($verLine -match '"(\d+)') {
                $major = [int]$Matches[1]
                if ($major -eq 1) { $major = 8 }   # old "1.8.0_xxx" style string
                if ($major -ge 21) { return $exe }
            }
        } catch { } finally { $ErrorActionPreference = $prevPref }
    }
    return $null
}

Clear-Host
Write-Host ""
Write-Host "  ============================================================"
Write-Host "   Web Based Tour Guide - automatic setup"
Write-Host "   Group 2026-Y2-S1-MLB-WEB3G1-03"
Write-Host "  ============================================================"

# the project root is the folder above this script
$root     = Split-Path -Parent $PSScriptRoot
$backend  = Join-Path $root 'backend'
$frontend = Join-Path $root 'frontend'
$dbFolder = Join-Path $root 'database'

if (-not (Test-Path $backend))  { Die "Cannot find the backend folder. Keep SETUP.bat inside the tourguide-app folder." }
if (-not (Test-Path $dbFolder)) { Die "Cannot find the database folder. Keep SETUP.bat inside the tourguide-app folder." }

$appPassword = 'TourGuide@2026'
$appLogin    = 'tourguide_app'

# ------------------------------------------------------ 1. check a JDK -----
Step 1 'Checking for a Java 21+ JDK'
$javaExe = Find-Java21
if (-not $javaExe) {
    Die "A Java 21 (or newer) JDK was not found.`n         Install a JDK from https://adoptium.net (Temurin 21 LTS) then open a NEW window and try again.`n         If you already installed one, set JAVA_HOME to its install folder."
}
$prevPref = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$javaVerLine = & $javaExe -version 2>&1 | Select-Object -First 1
$ErrorActionPreference = $prevPref
Ok "Found $javaVerLine"

# ------------------------------------------- 2. find the SQL Server instance ---
Step 2 'Looking for SQL Server'

$instanceKey = 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\Instance Names\SQL'
if (-not (Test-Path $instanceKey)) {
    Die "SQL Server does not appear to be installed on this computer.`n         Install SQL Server Developer Edition (free) and SSMS, then run this again."
}

$instanceProps = Get-ItemProperty $instanceKey
$instanceNames = $instanceProps.PSObject.Properties |
                 Where-Object { $_.Name -notlike 'PS*' } |
                 Select-Object -ExpandProperty Name

if (-not $instanceNames) { Die "SQL Server is registered but no instance was found." }

# prefer the default instance if there is one
if ($instanceNames -contains 'MSSQLSERVER') { $instanceName = 'MSSQLSERVER' }
else { $instanceName = @($instanceNames)[0] }

$regFolder = $instanceProps.$instanceName          # e.g. MSSQL16.SQLEXPRESS

if ($instanceName -eq 'MSSQLSERVER') {
    $serviceName  = 'MSSQLSERVER'
    $localAddress = 'localhost'                     # for shared-memory setup
    $envServer    = 'localhost'                     # for the app, over TCP
} else {
    $serviceName  = 'MSSQL$' + $instanceName
    $localAddress = "localhost\$instanceName"
    $envServer    = 'localhost'
}

Ok "Found instance '$instanceName' (service $serviceName)"

$service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
if (-not $service) { Die "The SQL Server service '$serviceName' was not found." }
if ($service.Status -ne 'Running') {
    Say 'Service is stopped - starting it'
    Start-Service $serviceName
    Start-Sleep -Seconds 5
}
Ok 'SQL Server service is running'

# --------------------------------------------- 3. run the database scripts ---
Step 3 'Creating the database'

function New-SqlConnection {
    param([string]$Database = 'master')
    $cs = "Server=$localAddress;Database=$Database;Integrated Security=True;TrustServerCertificate=True;Connect Timeout=30"
    $c = New-Object System.Data.SqlClient.SqlConnection $cs
    $c.Open()
    return $c
}

function Invoke-SqlText {
    param($Connection, [string]$Text)
    # split on GO exactly the way SSMS does
    $batches = [regex]::Split($Text, '(?im)^[ \t]*GO[ \t]*;?[ \t]*$')
    foreach ($batch in $batches) {
        if ([string]::IsNullOrWhiteSpace($batch)) { continue }
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText    = $batch
        $cmd.CommandTimeout = 180
        [void]$cmd.ExecuteNonQuery()
    }
}

try {
    $conn = New-SqlConnection
} catch {
    Die "Could not connect to SQL Server as your Windows account.`n         $($_.Exception.Message)"
}

try {
    $schemaPath = Join-Path $dbFolder '01_schema.sql'
    $seedPath   = Join-Path $dbFolder '02_seed.sql'

    Say 'Running 01_schema.sql (tables)'
    Invoke-SqlText -Connection $conn -Text (Get-Content $schemaPath -Raw)
    Ok 'Tables created'

    Say 'Running 02_seed.sql (demo data)'
    Invoke-SqlText -Connection $conn -Text (Get-Content $seedPath -Raw)
    Ok 'Demo data inserted'
} catch {
    Die "A database script failed.`n         $($_.Exception.Message)"
}

# ------------------------------------------ 4. create the application login ---
Step 4 'Creating the application login'

$loginSql = @"
USE master;
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = '$appLogin')
    CREATE LOGIN [$appLogin] WITH PASSWORD = '$appPassword',
        CHECK_POLICY = OFF, DEFAULT_DATABASE = [TourGuideDB];
ELSE
    ALTER LOGIN [$appLogin] WITH PASSWORD = '$appPassword';
ALTER LOGIN [$appLogin] ENABLE;

USE TourGuideDB;
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = '$appLogin')
    CREATE USER [$appLogin] FOR LOGIN [$appLogin];
ALTER ROLE db_owner ADD MEMBER [$appLogin];
"@

try {
    Invoke-SqlText -Connection $conn -Text $loginSql
    Ok "Login '$appLogin' created with rights on TourGuideDB"
} catch {
    Die "Could not create the application login.`n         $($_.Exception.Message)"
}
$conn.Close()

# ----------------------------------- 5. enable TCP/IP and mixed-mode login ---
Step 5 'Enabling TCP/IP on port 1433'

$sqlRegBase = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\$regFolder\MSSQLServer"
$tcpKey     = "$sqlRegBase\SuperSocketNetLib\Tcp"

try {
    Set-ItemProperty -Path $tcpKey -Name 'Enabled' -Value 1
    Set-ItemProperty -Path "$tcpKey\IPAll" -Name 'TcpPort' -Value '1433'
    Set-ItemProperty -Path "$tcpKey\IPAll" -Name 'TcpDynamicPorts' -Value ''
    Ok 'TCP/IP enabled on port 1433'

    Set-ItemProperty -Path $sqlRegBase -Name 'LoginMode' -Value 2
    Ok 'Mixed-mode authentication enabled'
} catch {
    Die "Could not change the SQL Server settings. Make sure you ran SETUP.bat as Administrator.`n         $($_.Exception.Message)"
}

Say 'Restarting SQL Server so the changes take effect'
try {
    Restart-Service -Name $serviceName -Force
    Start-Sleep -Seconds 6
    Ok 'SQL Server restarted'
} catch {
    Die "Could not restart the SQL Server service.`n         $($_.Exception.Message)"
}

Say 'Checking that port 1433 is listening'
$listening = $false
foreach ($try in 1..10) {
    $test = Test-NetConnection -ComputerName 'localhost' -Port 1433 -WarningAction SilentlyContinue
    if ($test.TcpTestSucceeded) { $listening = $true; break }
    Start-Sleep -Seconds 2
}
if ($listening) { Ok 'Port 1433 is open' }
else { Warn 'Port 1433 is not answering yet. Give it a minute, then start the app.' }

# ----------------------------------------------------- 6. write backend\.env ---
Step 6 'Writing backend\.env'

$javaHomeForApp = Split-Path -Parent (Split-Path -Parent $javaExe)   # .../bin/java.exe -> .../<jdk root>

$envPath = Join-Path $backend '.env'
$envText = @"
# Generated by setup\setup.ps1 - safe to edit by hand

PORT=5000

DB_SERVER=$envServer
DB_PORT=1433
DB_NAME=TourGuideDB
DB_USER=$appLogin
DB_PASSWORD=$appPassword
DB_ENCRYPT=false
DB_TRUST_SERVER_CERTIFICATE=true

JWT_SECRET=$([guid]::NewGuid().ToString() + [guid]::NewGuid().ToString())
JWT_EXPIRES_IN=7d

CLIENT_ORIGIN=http://localhost:5173

# The JDK 21+ START.bat should launch the backend with - avoids picking up an
# older 'java' that might come first on PATH (e.g. a JRE bundled with another app).
JAVA_HOME=$javaHomeForApp
"@

if (Test-Path $envPath) {
    Copy-Item $envPath "$envPath.backup" -Force
    Say 'An existing .env was backed up to .env.backup'
}
Set-Content -Path $envPath -Value $envText -Encoding ASCII
Ok '.env written'

# ------------------------------------------- 7. build backend, install npm ---
Step 7 'Building the backend and installing frontend packages (this takes a few minutes)'

Push-Location $backend
Say 'backend  - .\mvnw.cmd -q -DskipTests package'
$env:JAVA_HOME = (Split-Path -Parent (Split-Path -Parent $javaExe))
& .\mvnw.cmd -q -DskipTests package
if ($LASTEXITCODE -ne 0) { Pop-Location; Die 'The Maven build failed in the backend folder. Scroll up for the compiler error.' }
Pop-Location
Ok 'Backend built (backend\target\tourguide-backend.jar)'

Push-Location $frontend
Say 'frontend - npm install'
& npm install --no-fund --no-audit
if ($LASTEXITCODE -ne 0) { Pop-Location; Die 'npm install failed in the frontend folder.' }
Pop-Location
Ok 'Frontend packages installed'

# --------------------------------------------------------------- finished ---
Write-Host ""
Write-Host "  ============================================================" -ForegroundColor Green
Write-Host "   Setup finished." -ForegroundColor Green
Write-Host "  ============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "   Now double-click START.bat to run the application."
Write-Host "   It opens at  http://localhost:5173"
Write-Host ""
Write-Host "   Log in with any of these (password: Password@123)"
Write-Host "     kasun.guide@mail.com    - tour guide"
Write-Host "     emma.tourist@mail.com   - tourist"
Write-Host "     admin@tourguide.lk      - administrator"
Write-Host ""
Read-Host "  Press Enter to close"
