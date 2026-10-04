# Builds the debug APK, installs it on the phone connected over USB and opens the app.
# Usage: pwsh scripts/install-debug.ps1   (add -SkipBuild to reinstall the last build)
param([switch]$SkipBuild)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

# The project builds with the JDK bundled with Android Studio; any other JAVA_HOME is only used
# when that one is missing and the path actually holds a JDK.
$studioJdk = 'C:\Program Files\Android\Android Studio\jbr'
if (Test-Path (Join-Path $studioJdk 'bin\java.exe')) {
    $env:JAVA_HOME = $studioJdk
} elseif (-not ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe')))) {
    Write-Host "No JDK found. Install Android Studio or set JAVA_HOME to a JDK 21 folder (without \bin)." -ForegroundColor Red
    exit 1
}
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
$package = 'io.github.perroabuelo.materialeleven'

$devices = & $adb devices | Select-String -Pattern '\tdevice$'
if (-not $devices) {
    Write-Host 'No phone found. Connect it over USB and accept the debugging prompt.' -ForegroundColor Red
    exit 1
}

if (-not $SkipBuild) {
    Write-Host '==> Building the debug APK' -ForegroundColor Cyan
    & .\gradlew.bat assembleDebug --console=plain -q
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host '==> Installing. On the phone, tap "Install" before the countdown ends.' -ForegroundColor Yellow
& $adb install -r $apk
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host '==> Opening Material Eleven' -ForegroundColor Cyan
& $adb shell am start -n "$package/.MainActivity" | Out-Null
Write-Host 'Done.' -ForegroundColor Green
