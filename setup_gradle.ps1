$url = "https://services.gradle.org/distributions/gradle-8.4-bin.zip"
$output = "gradle-8.4-bin.zip"
$dir = "gradle_dist"

if (-not (Test-Path $output)) {
    Write-Host "Downloading Gradle 8.4 using curl..."
    # Use curl.exe directly
    & curl.exe -L -o $output $url
}

if (Test-Path $output) {
    $size = (Get-Item $output).Length
    if ($size -lt 1000000) {
        Write-Error "Downloaded file is too small ($size bytes). Download likely failed."
        exit 1
    }
} else {
    Write-Error "Download failed. File not found."
    exit 1
}

if (-not (Test-Path "$dir\gradle-8.4\bin\gradle.bat")) {
    Write-Host "Extracting Gradle..."
    Expand-Archive -Path $output -DestinationPath $dir -Force
}

Write-Host "Gradle setup complete."
