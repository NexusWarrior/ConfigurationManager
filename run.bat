@echo off
setlocal
set "PROJECT_DIR=%~dp0"
set "GRADLE_VERSION=9.6.0"
set "GRADLE_HOME=%PROJECT_DIR%.gradle-bootstrap\gradle-%GRADLE_VERSION%"
where gradle >nul 2>nul
if %errorlevel% equ 0 (
    set "GRADLE_COMMAND=gradle"
) else (
    set "GRADLE_COMMAND=%GRADLE_HOME%\bin\gradle.bat"
    if not exist "%GRADLE_HOME%\bin\gradle.bat" (
        powershell -NoProfile -Command ^
          "$root='%PROJECT_DIR%.gradle-bootstrap';" ^
          "New-Item -ItemType Directory -Force $root | Out-Null;" ^
          "$zip=Join-Path $root 'gradle.zip';" ^
          "$url='https://services.gradle.org/distributions/';" ^
          "$url+='gradle-%GRADLE_VERSION%-bin.zip';" ^
          "Invoke-WebRequest $url -OutFile $zip;" ^
          "Expand-Archive -Force $zip $root"
    )
)
if "%~1"=="test" (
    call "%GRADLE_COMMAND%" --project-dir "%PROJECT_DIR%" test
) else (
    call "%GRADLE_COMMAND%" --project-dir "%PROJECT_DIR%" run --args="%*"
)
exit /b %errorlevel%
