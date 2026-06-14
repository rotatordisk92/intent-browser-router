# IntentBrowserRouter Build & Sign Script for Windows
# Usage: .\build-and-sign.ps1

$ErrorActionPreference = "Stop"

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildDir = "$ProjectDir\app\build"
$OutputAPK = "$BuildDir\outputs\apk\release\app-release.apk"
$KeystoreFile = "$ProjectDir\keystore.jks"
$KeystorePassword = "android"
$KeyAlias = "android"
$KeyPassword = "android"

Write-Host "📦 IntentBrowserRouter Build & Sign" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan

# Check if keystore exists, create if not
if (-not (Test-Path $KeystoreFile)) {
    Write-Host "🔑 Creating keystore..." -ForegroundColor Yellow
    $dname = "CN=IntentBrowserRouter, O=Developer, C=US"
    
    # Using keytool (requires JDK to be installed)
    $cmd = @(
        'keytool',
        '-genkey',
        '-v',
        '-keystore', $KeystoreFile,
        '-keyalg', 'RSA',
        '-keysize', '2048',
        '-validity', '10000',
        '-alias', $KeyAlias,
        '-storepass', $KeystorePassword,
        '-keypass', $KeyPassword,
        '-dname', "`"$dname`""
    )
    
    & $cmd 2>&1 | Out-Null
}

# Build release APK
Write-Host "🔨 Building release APK..." -ForegroundColor Yellow
Push-Location $ProjectDir

if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat assembleRelease
} else {
    gradle assembleRelease
}

Pop-Location

if (-not (Test-Path $OutputAPK)) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

# Sign APK
Write-Host "✍️  Signing APK..." -ForegroundColor Yellow
$cmd = @(
    'jarsigner',
    '-verbose',
    '-sigalg', 'SHA1withRSA',
    '-digestalg', 'SHA1',
    '-keystore', $KeystoreFile,
    '-storepass', $KeystorePassword,
    '-keypass', $KeyPassword,
    $OutputAPK,
    $KeyAlias
)

& $cmd 2>&1 | Out-Null

# Verify signature
Write-Host "✅ Verifying signature..." -ForegroundColor Yellow
jarsigner -verify -verbose $OutputAPK 2>&1 | Out-Null

Write-Host ""
Write-Host "✨ Success!" -ForegroundColor Green
Write-Host "📱 APK ready: $OutputAPK" -ForegroundColor Green
Write-Host ""
Write-Host "To install on your device:" -ForegroundColor Cyan
Write-Host "  adb install -r $OutputAPK" -ForegroundColor Gray
