REM ----- Only set CARBON_HOME if not already set ----------------------------
setlocal enabledelayedexpansion

REM Read the file path as a command line argument.
FILE_PATH=%1
if "%FILE_PATH%"=="" (
    echo "File path is not set. Please set file path."
    goto:end
)

REM %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%HOME_DIR%"=="" set HOME_DIR=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%HOME_DIR:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

REM Read the file and get the values.
FOR /F "tokens=1,2,3,4 delims=," %%a in (%FILE_PATH%) do (
    set username=%%a
    set domainName=%%b
    set tenantDomain=%%c
    set tenantId=%%d
    call %HOME_DIR%\bin\forget-me.bat -U %username% -D %domainName% -T %tenantDomain% -TID %tenantId%
)
:end