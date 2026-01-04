@echo off
echo ====================================
echo Library Management System - Install
echo ====================================
echo.

echo Checking environment...
echo.

REM Check npm
where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] npm not found, please install Node.js first
    echo Download: https://nodejs.org/
    pause
    exit /b 1
)

echo [OK] npm found
echo.

echo Installing dependencies with npm...
echo.

npm install

if %errorlevel% equ 0 (
    echo.
    echo ====================================
    echo [SUCCESS] Installation completed!
    echo ====================================
    echo.
    echo Run the project:
    echo   npm run dev
    echo.
    echo Test accounts:
    echo   Admin: admin / admin123
    echo   User: 2021001 / 123456
    echo.
) else (
    echo.
    echo ====================================
    echo [FAILED] Installation failed!
    echo ====================================
    echo.
    echo Try:
    echo 1. Run as Administrator
    echo 2. Disable antivirus temporarily
    echo 3. Check network connection
    echo 4. Use yarn: npm install -g yarn then yarn
    echo.
)

pause
