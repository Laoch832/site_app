# Android Hello World Project

This project contains a complete Android "Hello World" application setup as per your requirements.

## 1. Environment Setup
Before building the application, ensure you have the following installed:
- **Android Studio** (2023.2.1 or higher)
- **JDK 17** or higher
- **Android SDK Platform 34**

## 2. Project Structure
The project has been initialized with the following configuration:
- **Package Name**: `com.example.helloworld`
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Build Type**: Release with signing configuration

## 3. Generating Signing Key
Before building the release APK, you must generate the keystore file.
1. Ensure `keytool` (from JDK) is in your system PATH.
2. Run the provided script:
   ```cmd
   generate_keystore.bat
   ```
   Or run the command manually:
   ```cmd
   keytool -genkey -v -keystore app/release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0 -storepass password123 -keypass password123 -dname "CN=Android Debug,O=Android,C=US"
   ```

## 4. Building the APK
You can build the APK using Android Studio or Gradle.

### Option A: Using Android Studio
1. Open this directory in Android Studio.
2. Wait for Gradle sync to complete.
3. Go to **Build > Generate Signed Bundle / APK**.
4. Select **APK** and choose the `release` build variant.
   (Note: The `build.gradle.kts` is already configured with the signing config, so you can also just run the `assembleRelease` task from the Gradle tool window).

### Option B: Using Command Line (if Gradle is installed)
```cmd
gradle assembleRelease
```

## 5. Output
The APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

## 6. Installation & Verification
To install and test the app:
1. Connect your device or start an emulator.
2. Run:
   ```cmd
   adb install app/build/outputs/apk/release/app-release.apk
   ```
3. The app "HelloWorld" should appear. Open it to see the "Hello World!" message.
