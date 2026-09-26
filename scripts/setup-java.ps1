$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$projectRoot = Split-Path $PSScriptRoot -Parent
$toolsDir = Join-Path $projectRoot '.tools'
$jdkDir = Join-Path $toolsDir 'jdk-17.0.20.1+1'
if (Test-Path (Join-Path $jdkDir 'bin\javac.exe')) {
    & (Join-Path $jdkDir 'bin\java.exe') -version
    exit $LASTEXITCODE
}
$downloadDir = Join-Path $toolsDir 'downloads'
New-Item -ItemType Directory -Force -Path $downloadDir | Out-Null
$archive = Join-Path $downloadDir 'temurin17.zip'
$downloadUrl = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.20.1%2B1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1.zip'
$expectedHash = 'e53a79c3c3d86865bd7e787903884331068e71321714ffd44f145785affc7cb0'
if (-not (Test-Path $archive)) { Invoke-WebRequest -Uri $downloadUrl -OutFile $archive }
if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash -ne $expectedHash) {
    throw 'JDK archive checksum mismatch; inspect .tools/downloads/temurin17.zip before retrying.'
}
Expand-Archive -LiteralPath $archive -DestinationPath $toolsDir
& (Join-Path $jdkDir 'bin\java.exe') -version
exit $LASTEXITCODE
