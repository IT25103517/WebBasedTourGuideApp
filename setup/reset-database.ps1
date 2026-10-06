<#
    Web Based Tour Guide - re-create the database with fresh demo data.
    Run RESET_DATABASE.bat instead of running this file directly.
#>

$ErrorActionPreference = 'Stop'

function Ok  ($m) { Write-Host "  [ OK ] $m" -ForegroundColor Green }
function Say ($m) { Write-Host "  $m" }
function Die ($m) {
    Write-Host ""
    Write-Host "  [STOP] $m" -ForegroundColor Red
    Write-Host ""
    Read-Host "  Press Enter to close"
    exit 1
}

Clear-Host
Write-Host ""
Write-Host "  Web Based Tour Guide - reset the database"
Write-Host "  -----------------------------------------"
Write-Host ""

$root     = Split-Path -Parent $PSScriptRoot
$dbFolder = Join-Path $root 'database'
if (-not (Test-Path $dbFolder)) { Die 'Cannot find the database folder.' }

$instanceKey = 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\Instance Names\SQL'
if (-not (Test-Path $instanceKey)) { Die 'SQL Server is not installed on this computer.' }

$props = Get-ItemProperty $instanceKey
$names = $props.PSObject.Properties | Where-Object { $_.Name -notlike 'PS*' } | Select-Object -ExpandProperty Name
if ($names -contains 'MSSQLSERVER') { $instanceName = 'MSSQLSERVER' } else { $instanceName = @($names)[0] }
if ($instanceName -eq 'MSSQLSERVER') { $address = 'localhost' } else { $address = "localhost\$instanceName" }

Write-Host "  This will DELETE all data in TourGuideDB and rebuild it"
Write-Host "  with the original demo accounts, packages and bookings."
Write-Host ""
$answer = Read-Host "  Type YES to continue"
if ($answer -ne 'YES') { Write-Host "`n  Cancelled - nothing was changed.`n"; Read-Host '  Press Enter to close'; exit 0 }

function Invoke-SqlText {
    param($Connection, [string]$Text)
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
    $cs = "Server=$address;Database=master;Integrated Security=True;TrustServerCertificate=True;Connect Timeout=30"
    $conn = New-Object System.Data.SqlClient.SqlConnection $cs
    $conn.Open()
} catch {
    Die "Could not connect to SQL Server.`n         $($_.Exception.Message)"
}

try {
    Say 'Closing other connections to TourGuideDB'
    Invoke-SqlText -Connection $conn -Text @"
IF DB_ID('TourGuideDB') IS NOT NULL
BEGIN
    ALTER DATABASE TourGuideDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    ALTER DATABASE TourGuideDB SET MULTI_USER;
END
"@

    Say 'Rebuilding the tables'
    Invoke-SqlText -Connection $conn -Text (Get-Content (Join-Path $dbFolder '01_schema.sql') -Raw)
    Ok 'Tables rebuilt'

    Say 'Inserting demo data'
    Invoke-SqlText -Connection $conn -Text (Get-Content (Join-Path $dbFolder '02_seed.sql') -Raw)
    Ok 'Demo data inserted'

    Say 'Restoring the application login'
    Invoke-SqlText -Connection $conn -Text @"
USE TourGuideDB;
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'tourguide_app')
    CREATE USER [tourguide_app] FOR LOGIN [tourguide_app];
ALTER ROLE db_owner ADD MEMBER [tourguide_app];
"@
    Ok 'Login restored'
} catch {
    Die "The reset failed.`n         $($_.Exception.Message)"
} finally {
    if ($conn.State -eq 'Open') { $conn.Close() }
}

Write-Host ""
Write-Host "  Database reset. All demo accounts use the password Password@123" -ForegroundColor Green
Write-Host ""
Read-Host "  Press Enter to close"
