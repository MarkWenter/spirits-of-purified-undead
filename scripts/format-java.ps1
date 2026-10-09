param([switch]$Fix, [string]$Java = 'java', [string]$Formatter)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
if (-not $Formatter) { $Formatter = Join-Path $root '.tools/format/google-java-format-1.24.0-all-deps.jar' }
$expected = '812F805F58112460EDF01BF202A8E61D0FD1F35C0D4FABD54220640776EC57A1'
if (-not (Test-Path -LiteralPath $Formatter)) { throw 'Download the pinned formatter documented in docs/CODE_STANDARDS.md first.' }
if ((Get-FileHash -LiteralPath $Formatter -Algorithm SHA256).Hash -ne $expected) { throw 'Formatter SHA-256 mismatch.' }
$flags = @('--aosp', '--skip-sorting-imports', '--skip-removing-unused-imports', '--skip-reflowing-long-strings', '--skip-javadoc-formatting')
if ($Fix) { $flags += '--replace' } else { $flags += @('--dry-run', '--set-exit-if-changed') }
$files = @(Get-ChildItem -LiteralPath (Join-Path $root 'src') -Filter '*.java' -Recurse -File | ForEach-Object FullName)
$failed = $false
for ($offset = 0; $offset -lt $files.Count; $offset += 60) {
    $last = [Math]::Min($offset + 59, $files.Count - 1)
    & $Java -jar $Formatter @flags $files[$offset..$last]
    if ($LASTEXITCODE -ne 0) { $failed = $true }
}
if ($Fix) {
    foreach ($file in $files) {
        $content = [IO.File]::ReadAllText($file).Replace("`r`n", "`n")
        [IO.File]::WriteAllText($file, $content, [Text.UTF8Encoding]::new($false))
    }
} else {
    foreach ($file in $files) {
        if ([IO.File]::ReadAllText($file).Contains("`r")) { $failed = $true; Write-Output "Expected LF: $file" }
    }
}
if ($failed) { throw 'Java formatting check failed.' }
Write-Output "Java format verified: $($files.Count) files."
