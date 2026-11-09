[CmdletBinding()]
param(
    [string]$SchemaPath = (Join-Path $PSScriptRoot '..\schema.sql'),
    [string]$SeedPath = (Join-Path $PSScriptRoot '..\seed.sql'),
    [string]$DbmlPath = (Join-Path $PSScriptRoot '..\..\docs\database\schema-v1.dbml')
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Assert-True {
    param(
        [Parameter(Mandatory)]
        [bool]$Condition,
        [Parameter(Mandatory)]
        [string]$Message
    )

    if (-not $Condition) {
        throw "Schema validation failed: $Message"
    }
}

$resolvedSchemaPath = (Resolve-Path -LiteralPath $SchemaPath).Path
$resolvedSeedPath = (Resolve-Path -LiteralPath $SeedPath).Path
$resolvedDbmlPath = (Resolve-Path -LiteralPath $DbmlPath).Path
$schemaEntry = Get-Content -Raw -LiteralPath $resolvedSchemaPath
$seed = Get-Content -Raw -LiteralPath $resolvedSeedPath
$dbml = Get-Content -Raw -LiteralPath $resolvedDbmlPath

$expectedSchemaModules = @(
    'schema/01_accounts.sql',
    'schema/02_social.sql',
    'schema/03_conversations.sql',
    'schema/04_messages.sql',
    'schema/05_moderation.sql',
    'schema/06_activity.sql'
)

$normalizedSchemaEntry = $schemaEntry -replace "`r`n", "`n"
$expectedIncludeBlock = ($expectedSchemaModules | ForEach-Object { "\ir $_" }) -join "`n"
Assert-True -Condition $normalizedSchemaEntry.Contains($expectedIncludeBlock) -Message 'schema modules are not included in dependency order'
Assert-True -Condition ($schemaEntry -notmatch '(?im)^\s*CREATE TABLE\b') -Message 'entry point must not duplicate module DDL'

foreach ($content in @($schemaEntry, $seed)) {
    Assert-True -Condition $content.Contains('\set ON_ERROR_STOP on') -Message 'SQL script must fail fast'
    Assert-True -Condition $content.Contains('BEGIN;') -Message 'SQL script must begin a transaction'
    Assert-True -Condition $content.Contains('COMMIT;') -Message 'SQL script must commit its transaction'
}

$schemaRoot = Split-Path -Parent $resolvedSchemaPath
$schemaModuleDirectory = Join-Path $schemaRoot 'schema'
$actualSchemaModuleNames = @(
    Get-ChildItem -LiteralPath $schemaModuleDirectory -Filter '*.sql' -File |
        Sort-Object Name |
        ForEach-Object { $_.Name }
)
$expectedSchemaModuleNames = @($expectedSchemaModules | ForEach-Object { Split-Path -Leaf $_ })
Assert-True -Condition (@(Compare-Object $expectedSchemaModuleNames $actualSchemaModuleNames).Count -eq 0) -Message 'schema module files differ from the six-file layout'

$schemaModuleContents = @(
    foreach ($relativeModulePath in $expectedSchemaModules) {
        Get-Content -Raw -LiteralPath (Join-Path $schemaRoot $relativeModulePath)
    }
)
$schema = (@($schemaEntry) + $schemaModuleContents) -join "`n"

$requiredTables = @(
    'users',
    'user_public_keys',
    'login_history',
    'friend_requests',
    'friendships',
    'user_blocks',
    'conversations',
    'direct_conversations',
    'conversation_members',
    'conversation_key_versions',
    'conversation_key_envelopes',
    'messages',
    'message_deletions',
    'spam_reports',
    'user_activity_logs'
)

$schemaTables = @(
    [regex]::Matches($schema, '(?im)^\s*CREATE TABLE\s+([a-z_]+)\s*\(') |
        ForEach-Object { $_.Groups[1].Value }
)
$dbmlTables = @(
    [regex]::Matches($dbml, '(?im)^\s*Table\s+([a-z_]+)\s*\{') |
        ForEach-Object { $_.Groups[1].Value }
)

Assert-True -Condition (@(Compare-Object ($requiredTables | Sort-Object) ($schemaTables | Sort-Object)).Count -eq 0) -Message 'schema table names differ from the V1 model'
Assert-True -Condition (@(Compare-Object ($requiredTables | Sort-Object) ($dbmlTables | Sort-Object)).Count -eq 0) -Message 'DBML table names differ from the V1 model'

Assert-True -Condition ($schema -notmatch '(?im)^\s*CREATE\s+(TRIGGER|FUNCTION|PROCEDURE)\b') -Message 'triggers, functions, and procedures are outside V1 scope'
Assert-True -Condition ($schema -notmatch '(?im)^\s*CREATE\s+EXTENSION\b') -Message 'unexpected extension dependency detected'
Assert-True -Condition ($schema -notmatch '(?im)\bpassword\s+(VARCHAR|TEXT)\b') -Message 'plaintext password column detected'
Assert-True -Condition ($schema -notmatch '(?im)\b(friend_count|friends_count|app_open_count|chatted_users_count|chatted_groups_count)\b') -Message 'calculated counter column detected'
$nonceIndexPattern = '(?is)CREATE\s+UNIQUE\s+INDEX\s+uq_messages_conversation_key_nonce\s+ON\s+messages\s*\(\s*conversation_id\s*,\s*encryption_key_version\s*,\s*encryption_nonce\s*\)\s+WHERE\s+content_encrypted\s+AND\s+encryption_key_version\s+IS\s+NOT\s+NULL\s+AND\s+encryption_nonce\s+IS\s+NOT\s+NULL\s*;'
Assert-True -Condition ($schema -match $nonceIndexPattern) -Message 'encrypted-message nonce safeguard is missing or has the wrong scope'
Assert-True -Condition $dbml.Contains('uq_messages_conversation_key_nonce') -Message 'DBML is missing the encrypted-message nonce index annotation'

Write-Output 'Schema validation passed.'
Write-Output "Tables: $($schemaTables.Count)"
Write-Output 'Schema modules and DBML table names are synchronized.'
