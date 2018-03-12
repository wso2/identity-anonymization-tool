REM
REM Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM WSO2 Inc. licenses this file to you under the Apache License,
REM Version 2.0 (the "License"); you may not use this file except
REM in compliance with the License.
REM You may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied. See the License for the
REM specific language governing permissions and limitations
REM under the License.
REM

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