@echo off
REM IntentBrowserRouter Build & Sign Script
REM Requires: Java 11+, Android SDK

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0
set BUILD_DIR=%PROJECT_DIR%app\build
set OUTPUT_APK=%BUILD_DIR%\outputs\apk\release\app-release.apk
set KEYSTORE_FILE=%PROJECT_DIR%keystore.jks
set KEYSTORE_PASSWORD=android
set KEY_ALIAS=android
set KEY_PASSWORD=android

echo 📦 IntentBrowserRouter Build ^& Sign
echo ==================================

REM Check if keystore exists
if not exist "%KEYSTORE_FILE%" (
    echo 🔑 Creating keystore...
    keytool -genkey -v -keystore "%KEYSTORE_FILE%" ^
        -keyalg RSA -keysize 2048 -validity 10000 ^
        -alias "%KEY_ALIAS%" ^
        -storepass "%KEYSTORE_PASSWORD%" ^
        -keypass "%KEY_PASSWORD%" ^
        -dname "CN=IntentBrowserRouter, O=Developer, C=US"
)

REM Build release APK
echo 🔨 Building release APK...
cd /d "%PROJECT_DIR%"

REM Try gradlew first, then gradle
if exist "gradlew.bat" (
    call gradlew.bat assembleRelease
) else (
    where gradle >nul 2>&1
    if errorlevel 1 (
        echo ❌ Neither gradlew nor gradle found. Please install Android Studio or Gradle.
        exit /b 1
    )
    gradle assembleRelease
)

if not exist "%OUTPUT_APK%" (
    echo ❌ Build failed
    exit /b 1
)

echo ✍️  Signing APK...
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 ^
    -keystore "%KEYSTORE_FILE%" ^
    -storepass "%KEYSTORE_PASSWORD%" ^
    -keypass "%KEY_PASSWORD%" ^
    "%OUTPUT_APK%" "%KEY_ALIAS%"

echo ✅ Verifying signature...
jarsigner -verify -verbose "%OUTPUT_APK%"

echo.
echo ✨ Success!
echo 📱 APK ready: %OUTPUT_APK%
echo.
echo To install on your device:
echo   adb install -r "%OUTPUT_APK%"

endlocal
