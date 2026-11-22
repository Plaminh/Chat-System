$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$releaseDirectory = Join-Path $projectRoot 'release'

Push-Location $projectRoot
try {
    & mvn clean verify
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed with exit code $LASTEXITCODE" }

    New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot 'admin-client/target/admin.jar') -Destination (Join-Path $releaseDirectory 'admin.jar') -Force
    Copy-Item -LiteralPath (Join-Path $projectRoot 'user-client/target/user.jar') -Destination (Join-Path $releaseDirectory 'user.jar') -Force
    Copy-Item -LiteralPath (Join-Path $projectRoot 'server/target/server.jar') -Destination (Join-Path $releaseDirectory 'server.jar') -Force

    $configurationFiles = @(
        @{ Source = 'config/client.properties.example'; Destination = 'client.properties' },
        @{ Source = 'config/server.properties.example'; Destination = 'server.properties' }
    )
    foreach ($configuration in $configurationFiles) {
        $destination = Join-Path $releaseDirectory $configuration.Destination
        if (-not (Test-Path -LiteralPath $destination)) {
            Copy-Item -LiteralPath (Join-Path $projectRoot $configuration.Source) -Destination $destination
        } else {
            Write-Host "Preserved existing $($configuration.Destination)"
        }
    }
    Write-Host "Release created at $releaseDirectory"
    Write-Host "Edit release/server.properties for the database. A repository-root .env, when present, overrides it."
} finally {
    Pop-Location
}
