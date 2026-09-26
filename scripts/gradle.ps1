# Project-local Java and Gradle cache; global Java settings are left untouched.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$previousJava = $env:JAVA_HOME
$previousGradle = $env:GRADLE_USER_HOME
try {
    $env:JAVA_HOME = Join-Path $projectRoot '.tools\jdk-17.0.20.1+1'
    $env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-user-home'
    if (-not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        throw 'Project JDK is missing. See docs/SETUP.md.'
    }
    Push-Location $projectRoot
    try {
        & (Join-Path $projectRoot 'gradlew.bat') @args
        $gradleExit = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:GRADLE_USER_HOME = $previousGradle
}
exit $gradleExit
