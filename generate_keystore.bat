@echo off
echo Generating Keystore...
"D:\REDHAT_java\bin\keytool.exe" -genkey -v -keystore app/release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0 -storepass password123 -keypass password123 -dname "CN=Android Debug,O=Android,C=US"
if %errorlevel% neq 0 (
    echo Error: keytool command failed.
    pause
    exit /b %errorlevel%
)
echo Keystore generated successfully at app/release-key.jks
