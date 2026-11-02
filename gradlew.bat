@echo off
setlocal EnableExtensions
set "GRADLE_VERSION=9.3.1"
set "GRADLE_SHA256=b266d5ff6b90eada6dc3b20cb090e3731302e553a27c5d3e4df1f0d76beaff06"
set "BOOT_DIR=%USERPROFILE%\.gradle\recentdock-bootstrap"
set "GRADLE_HOME=%BOOT_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_PATH=%BOOT_DIR%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    if not exist "%BOOT_DIR%" mkdir "%BOOT_DIR%"
    echo [RecentDock] Downloading Gradle %GRADLE_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "$ErrorActionPreference='Stop';" ^
      "$url='https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip';" ^
      "$zip='%ZIP_PATH%';" ^
      "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
      "$hash=(Get-FileHash -Algorithm SHA256 $zip).Hash.ToLowerInvariant();" ^
      "if($hash -ne '%GRADLE_SHA256%'){ throw 'Gradle SHA-256 mismatch: ' + $hash };" ^
      "Expand-Archive -Path $zip -DestinationPath '%BOOT_DIR%' -Force;"
    if errorlevel 1 exit /b 1
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
