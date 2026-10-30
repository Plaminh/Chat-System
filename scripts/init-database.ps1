[CmdletBinding()]
param(
    [ValidateNotNullOrEmpty()]
    [string]$HostName = 'localhost',

    [ValidateRange(1, 65535)]
    [int]$Port = 5432,

    [ValidatePattern('^[A-Za-z][A-Za-z0-9_]{0,62}$')]
    [string]$DatabaseName = 'chat_system_dev',

    [ValidateNotNullOrEmpty()]
    [string]$Username = 'postgres',

    [ValidatePattern('^[A-Za-z][A-Za-z0-9_]{0,62}$')]
    [string]$MaintenanceDatabase = 'postgres',

    [switch]$LoadSeed,

    [switch]$UseExistingEmpty
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Resolve-PostgreSqlTool {
    param(
        [Parameter(Mandatory)]
        [string]$Name
    )

    $command = Get-Command $Name -CommandType Application -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        throw "Required PostgreSQL tool '$Name' was not found on PATH. Install PostgreSQL command-line tools and retry."
    }
    return $command.Source
}

function Invoke-PostgreSqlTool {
    param(
        [Parameter(Mandatory)]
        [string]$Tool,
        [Parameter(Mandatory)]
        [string[]]$Arguments,
        [Parameter(Mandatory)]
        [string]$Operation
    )

    & $Tool @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Operation failed with exit code $LASTEXITCODE. Review the PostgreSQL output above."
    }
}

function Invoke-PostgreSqlQuery {
    param(
        [Parameter(Mandatory)]
        [string]$Psql,
        [Parameter(Mandatory)]
        [string]$Database,
        [Parameter(Mandatory)]
        [string]$Query,
        [Parameter(Mandatory)]
        [string]$Operation
    )

    $output = & $Psql -X -A -t -v ON_ERROR_STOP=1 -h $HostName -p $Port -U $Username -d $Database -c $Query
    if ($LASTEXITCODE -ne 0) {
        throw "$Operation failed with exit code $LASTEXITCODE. Review the PostgreSQL output above."
    }
    return ($output -join "`n").Trim()
}

$repositoryRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$schemaPath = (Resolve-Path -LiteralPath (Join-Path $repositoryRoot 'script\schema.sql')).Path
$seedPath = (Resolve-Path -LiteralPath (Join-Path $repositoryRoot 'script\seed.sql')).Path
$psql = Resolve-PostgreSqlTool -Name 'psql'
$createdb = Resolve-PostgreSqlTool -Name 'createdb'

Write-Host "PostgreSQL target: $Username@$HostName`:$Port/$DatabaseName"
Write-Host 'Authentication is delegated to standard PostgreSQL mechanisms (prompt, pgpass, or environment).'

$databaseExistsQuery = "SELECT 1 FROM pg_catalog.pg_database WHERE datname = '$DatabaseName';"
$databaseExists = (Invoke-PostgreSqlQuery -Psql $psql -Database $MaintenanceDatabase -Query $databaseExistsQuery -Operation 'Database existence check') -eq '1'

if ($databaseExists) {
    if (-not $UseExistingEmpty) {
        throw "Database '$DatabaseName' already exists. No changes were made. Use -UseExistingEmpty only after confirming this is the intended empty development database."
    }

    $userTableCountQuery = "SELECT count(*) FROM pg_catalog.pg_tables WHERE schemaname NOT IN ('pg_catalog', 'information_schema');"
    $userTableCountText = Invoke-PostgreSqlQuery -Psql $psql -Database $DatabaseName -Query $userTableCountQuery -Operation 'Existing database safety check'
    $userTableCount = 0
    if (-not [int]::TryParse($userTableCountText, [ref]$userTableCount)) {
        throw "Could not interpret existing database table count '$userTableCountText'. No schema was applied."
    }
    if ($userTableCount -ne 0) {
        throw "Database '$DatabaseName' contains $userTableCount user table(s). Refusing to overwrite or merge an existing schema."
    }

    Write-Host "Using explicitly approved empty database '$DatabaseName'."
} else {
    Write-Host "Creating UTF-8 database '$DatabaseName'..."
    Invoke-PostgreSqlTool -Tool $createdb -Arguments @(
        '-h', $HostName,
        '-p', $Port,
        '-U', $Username,
        '--encoding=UTF8',
        '--template=template0',
        $DatabaseName
    ) -Operation 'Database creation'
}

Write-Host "Applying schema from '$schemaPath'..."
Invoke-PostgreSqlTool -Tool $psql -Arguments @(
    '-X',
    '-v', 'ON_ERROR_STOP=1',
    '-h', $HostName,
    '-p', $Port,
    '-U', $Username,
    '-d', $DatabaseName,
    '-f', $schemaPath
) -Operation 'Schema application'

if ($LoadSeed) {
    Write-Host "Loading fictional development data from '$seedPath'..."
    Invoke-PostgreSqlTool -Tool $psql -Arguments @(
        '-X',
        '-v', 'ON_ERROR_STOP=1',
        '-h', $HostName,
        '-p', $Port,
        '-U', $Username,
        '-d', $DatabaseName,
        '-f', $seedPath
    ) -Operation 'Seed loading'
}

Write-Host "Database V1 initialization completed for '$DatabaseName'."
