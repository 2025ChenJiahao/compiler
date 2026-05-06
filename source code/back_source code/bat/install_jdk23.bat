@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  JDK 23 Installer for Windows
echo ========================================
echo.
echo This script will:
echo   1. Check if Java 23 is already installed
echo   2. Download Adoptium Temurin JDK 23
echo   3. Install JDK silently
echo   4. Set JAVA_HOME environment variable
echo   5. Add Java to PATH
echo.

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] This script requires administrator privileges!
    echo [INFO] Right-click and select "Run as administrator"
    echo.
    pause
    exit /b 1
)

echo [Step 1/5] Checking existing Java installation...
set "JAVA_FOUND=0"
set "JAVA_VERSION=0"

where java >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
        set "JAVA_VERSION=%%i"
        set "JAVA_VERSION=!JAVA_VERSION:~1,-1!"
    )
    for /f "tokens=1 delims=." %%i in ("!JAVA_VERSION!") do (
        if %%i geq 23 (
            set "JAVA_FOUND=1"
            echo [OK] Java !JAVA_VERSION! found (already 23+)
        ) else (
            echo [WARNING] Java !JAVA_VERSION! found (needs update)
        )
    )
) else (
    echo [INFO] No Java found, will install JDK 23
)
echo.

if !JAVA_FOUND! equ 1 (
    echo [INFO] Java 23+ is already installed. Exiting...
    echo.
    pause
    exit /b 0
)

echo [Step 2/5] Detecting system architecture...
set "ARCH=x64"
set "INSTALLER_URL=https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.1%2B11/OpenJDK23U-jdk_x64_windows_hotspot_23.0.1_11.msi"
set "INSTALLER_NAME=OpenJDK23U-jdk_x64_windows_hotspot_23.0.1_11.msi"

reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v PROCESSOR_ARCHITECTURE | findstr "x86" >nul 2>&1
if %errorlevel% equ 0 (
    set "ARCH=x86"
    set "INSTALLER_URL=https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23.0.1%2B11/OpenJDK23U-jdk_x86-32_windows_hotspot_23.0.1_11.msi"
    set "INSTALLER_NAME=OpenJDK23U-jdk_x86-32_windows_hotspot_23.0.1_11.msi"
)
echo [OK] Detected architecture: %ARCH%
echo [INFO] Download URL: %INSTALLER_URL%
echo.

echo [Step 3/5] Downloading JDK 23 installer...
echo [INFO] This may take a few minutes depending on your internet speed...

powershell -Command "Invoke-WebRequest -Uri '%INSTALLER_URL%' -OutFile '%INSTALLER_NAME%' -Verbose"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to download JDK installer!
    echo [INFO] Please check your internet connection and try again.
    echo.
    pause
    exit /b 1
)

echo [OK] Download completed: %INSTALLER_NAME%
echo.

echo [Step 4/5] Installing JDK 23...
echo [INFO] Installing silently...

msiexec /i "%INSTALLER_NAME%" /qn ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install JDK!
    echo [INFO] Please check the error message above.
    echo.
    pause
    exit /b 1
)

echo [OK] JDK 23 installed successfully!
echo.

echo [Step 5/5] Setting up environment variables...

set "JAVA_HOME="
for /d %%j in ("C:\Program Files\Eclipse Adoptium\jdk-23*") do (
    if exist "%%j\bin\java.exe" (
        set "JAVA_HOME=%%j"
        goto :java_home_found
    )
)
:java_home_found

if not defined JAVA_HOME (
    echo [ERROR] Could not find JDK installation path!
    echo [INFO] Please check the installation manually.
    echo.
    pause
    exit /b 1
)

echo [OK] Found JDK at: %JAVA_HOME%

setx JAVA_HOME "%JAVA_HOME%" /M
if %errorlevel% neq 0 (
    echo [ERROR] Failed to set JAVA_HOME environment variable!
    echo [INFO] Please set it manually.
) else (
    echo [OK] JAVA_HOME environment variable set
)

set "PATH_NEW=%JAVA_HOME%\bin;%PATH%"
setx PATH "%PATH_NEW%" /M
if %errorlevel% neq 0 (
    echo [ERROR] Failed to add Java to PATH!
    echo [INFO] Please add it manually.
) else (
    echo [OK] Java added to PATH
)
echo.

echo [INFO] Verifying installation...
set "PATH=%JAVA_HOME%\bin;%PATH%"

java -version
if %errorlevel% equ 0 (
    echo [SUCCESS] Java installation verified!
    echo [INFO] Java is now ready to use.
) else (
    echo [WARNING] Java verification failed!
    echo [INFO] Please restart your computer and try again.
)
echo.

echo [INFO] Cleaning up...
del "%INSTALLER_NAME%" >nul 2>&1
echo [OK] Cleanup completed.
echo.

echo ========================================
echo JDK 23 Installation Complete!
echo ========================================
echo.
echo What's next:
echo   1. Restart your computer to apply environment variables
echo   2. Run: java -version to verify
echo   3. Run: bat/run.bat to start your backend service
echo.
pause
exit /b 0