$gradleUrl = "https://services.gradle.org/distributions/gradle-8.4-bin.zip"
$gradleZip = "gradle-8.4-bin.zip"
$gradleDir = "gradle_dist"

$jdkUrl = "https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip"
$jdkZip = "jdk-17.zip"
$jdkDir = "jdk_dist"

$cmdlineUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$cmdlineZip = "commandlinetools.zip"
$sdkDir = "android_sdk"

# Setup Gradle
if (-not (Test-Path $gradleZip) -and -not (Test-Path "$gradleDir\gradle-8.4\bin\gradle.bat")) {
    Write-Host "Downloading Gradle 8.4..."
    & curl.exe -L -o $gradleZip $gradleUrl
}

if (Test-Path $gradleZip) {
    if (-not (Test-Path "$gradleDir\gradle-8.4\bin\gradle.bat")) {
        Write-Host "Extracting Gradle..."
        Expand-Archive -Path $gradleZip -DestinationPath $gradleDir -Force
    }
}

# Setup JDK 17
if (-not (Test-Path $jdkZip) -and -not (Test-Path "$jdkDir\jdk-17.0.2\bin\java.exe")) {
    Write-Host "Downloading JDK 17..."
    & curl.exe -L -o $jdkZip $jdkUrl
}

if (Test-Path $jdkZip) {
    if (-not (Test-Path "$jdkDir\jdk-17.0.2\bin\java.exe")) {
        Write-Host "Extracting JDK..."
        Expand-Archive -Path $jdkZip -DestinationPath $jdkDir -Force
    }
}

# Setup Android SDK
if (-not (Test-Path $cmdlineZip) -and -not (Test-Path "$sdkDir\cmdline-tools\bin\sdkmanager.bat")) {
    Write-Host "Downloading Android Command Line Tools..."
    & curl.exe -L -o $cmdlineZip $cmdlineUrl
}

if (Test-Path $cmdlineZip) {
    if (-not (Test-Path "$sdkDir\cmdline-tools\bin\sdkmanager.bat")) {
        Write-Host "Extracting Command Line Tools..."
        Expand-Archive -Path $cmdlineZip -DestinationPath $sdkDir -Force
    }
}

# Install SDK Packages
$sdkManager = "$PWD\$sdkDir\cmdline-tools\bin\sdkmanager.bat"
$javaExe = "$PWD\$jdkDir\jdk-17.0.2\bin\java.exe"
$env:JAVA_HOME = "$PWD\$jdkDir\jdk-17.0.2"

if (Test-Path $sdkManager) {
    Write-Host "Installing Android SDK packages (platforms;android-34, build-tools;34.0.0)..."
    $sdkRoot = "$PWD\$sdkDir"
    
    # Create yes file
    "y`ny`ny`ny`ny`ny`ny`ny`ny`ny`n" | Out-File -Encoding ASCII yes.txt
    
    # Run sdkmanager
    Get-Content yes.txt | & $sdkManager --sdk_root="$sdkRoot" "platforms;android-34" "build-tools;34.0.0"
}

# Create local.properties with forward slashes
$sdkPath = "$PWD\$sdkDir".Replace("\", "/")
"sdk.dir=$sdkPath" | Out-File -Encoding UTF8 local.properties

Write-Host "Environment setup complete."
