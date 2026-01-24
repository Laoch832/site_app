@echo off

echo Setting up environment...
powershell -ExecutionPolicy Bypass -File setup_env.ps1

if not exist jdk_dist\jdk-17.0.2\bin\java.exe (
    echo JDK setup failed!
    pause
    exit /b 1
)

set JAVA_HOME=%CD%\jdk_dist\jdk-17.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
set ANDROID_HOME=%CD%\android_sdk

if not exist gradle_dist\gradle-8.4\bin\gradle.bat (
    echo Gradle setup failed!
    pause
    exit /b 1
)

echo Stopping Gradle Daemon...
call gradle_dist\gradle-8.4\bin\gradle.bat --stop

echo Building Release APK...
call gradle_dist\gradle-8.4\bin\gradle.bat assembleRelease

if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b %errorlevel%
)

echo Build successful!
echo APK location: app\build\outputs\apk\release\app-release.apk