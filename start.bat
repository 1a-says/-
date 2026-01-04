@echo off
echo ====================================
echo Library Management System - Start
echo ====================================
echo.

REM Check node_modules
if not exist "node_modules" (
    echo [WARNING] Dependencies not found, please run install.bat first
    echo.
    pause
    exit /b 1
)

echo Starting development server...
echo.
echo Server will start at http://localhost:3000
echo Browser will auto-open
echo.
echo Test accounts:
echo   Admin: admin / admin123
echo   User: 2021001 / 123456
echo.
echo ====================================
echo.

npm run dev

pause
