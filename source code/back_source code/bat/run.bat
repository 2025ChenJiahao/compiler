@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  S-ONNXCompiler-Backend Runner
echo ========================================
echo.

echo [Step 1/4] Checking Java...
set "JAVA_HOME=C:\Program Files\Java\jdk-23"
set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"

if not exist "%JAVA_EXEC%" (
    echo [ERROR] Java 23 not found at: %JAVA_HOME%
    echo [INFO] Checking other Java versions...
    set "JAVA_FOUND=0"
    for /d %%j in ("C:\Program Files\Java\jdk-*", "C:\Program Files\Java\jdk1.*") do (
        if exist "%%j\bin\java.exe" (
            set "JAVA_HOME=%%j"
            set "JAVA_EXEC=%%j\bin\java.exe"
            set "JAVA_FOUND=1"
            goto :java_found
        )
    )
    :java_found
    if !JAVA_FOUND! equ 0 (
        echo [ERROR] No Java 23+ found!
        echo [INFO] Please install Java 23 from https://adoptium.net/
        echo.
        pause
        exit /b 1
    )
)
echo [OK] Found Java: %JAVA_HOME%
for /f "tokens=3" %%i in ('"%JAVA_EXEC%" -version 2^>^&1 ^| findstr "version"') do (
    set "JAVA_VERSION=%%i"
    echo [INFO] Java version: !JAVA_VERSION!
)
echo.

set "SCRIPT_DIR=%~dp0"
echo [Step 2/4] Checking script location...
echo [INFO] Script directory: %SCRIPT_DIR%

set "PROJECT_ROOT=%SCRIPT_DIR%..\"
set "JAR_PATH=%PROJECT_ROOT%target\bianyi-1.0-SNAPSHOT.jar"
echo [INFO] Project root: %PROJECT_ROOT%
echo [INFO] JAR path: %JAR_PATH%
echo.

echo [Step 3/4] Checking JAR file...
if not exist "%JAR_PATH%" (
    echo [ERROR] JAR file not found at: %JAR_PATH%
    echo [INFO] Checking if we're in project root...
    if exist "%SCRIPT_DIR%target\bianyi-1.0-SNAPSHOT.jar" (
        set "JAR_PATH=%SCRIPT_DIR%target\bianyi-1.0-SNAPSHOT.jar"
        echo [OK] Found JAR in current directory: %JAR_PATH%
    ) else (
        echo [ERROR] No JAR file found!
        echo [INFO] Please build the project first: mvn clean package
        echo.
        pause
        exit /b 1
    )
) else (
    echo [OK] Found JAR: %JAR_PATH%
)
echo.

echo [Step 4/4] Starting application...
echo ----------------------------------------
echo [INFO] Starting service with:
set "SERVICE_TITLE=S-ONNXCompiler-Backend"
echo [INFO]   Command: "%JAVA_EXEC%" -jar "%JAR_PATH%"
echo [INFO]   Window: %SERVICE_TITLE%
echo [INFO]   Log: Will open in new window

echo [INFO] Launching service...
start "%SERVICE_TITLE%" "%JAVA_EXEC%" -jar "%JAR_PATH%"

echo [INFO] Waiting for service to start...
set "COUNTER=0"
set "MAX_WAIT=30"
:wait_loop
if %COUNTER% geq %MAX_WAIT% (
    echo [ERROR] Service failed to start within %MAX_WAIT% seconds!
    echo [INFO] Check the service window for error messages
    echo.
    pause
    exit /b 1
)

tasklist | findstr "java" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java process is running
    goto :service_started
)

timeout /t 2 /nobreak >nul
set /a COUNTER=COUNTER+1
echo [INFO] Waiting... (%COUNTER%/%MAX_WAIT%)
goto :wait_loop

:service_started
echo.
echo [SUCCESS] Service started successfully!
echo ========================================
echo Service Information:
echo   Status: Running
echo   API Endpoint: http://localhost:8080/api/compiler/compile
echo   Health Check: http://localhost:8080/actuator/health
echo   Java: %JAVA_VERSION%
echo   JAR: %JAR_PATH%
echo ========================================
echo.
echo [INFO] To test the API:
echo   curl -X POST http://localhost:8080/api/compiler/compile ^
echo     -H "Content-Type: application/json" ^
echo     -d "{\"code\": \"ir_version: 7\"}"
echo.
echo [INFO] To stop the service:
echo   1. Press Ctrl+C in the service window
echo   2. Or use: taskkill /f /im java.exe /fi "windowtitle eq %SERVICE_TITLE%"
echo.
echo Service is now running in a separate window.
echo.
pause
exit /b 0