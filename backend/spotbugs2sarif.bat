@echo off
REM ---------------------------------------------------------------------
REM SpotBugs XML -> SARIF converter for GitLab SAST
REM Usage: spotbugs2sarif.bat input.xml output.json
REM Example: spotbugs2sarif.bat target/spotbugsXml.xml gl-sast-report.json
REM ---------------------------------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION

IF "%~1"=="" (
    echo Usage: %0 input.xml output.json
    exit /b 1
)
IF "%~2"=="" (
    echo Usage: %0 input.xml output.json
    exit /b 1
)

SET input=%~1
SET output=%~2

REM Check if input file exists
IF NOT EXIST "%input%" (
    echo Input file %input% not found!
    exit /b 1
)

REM Temporary JSON array
SET sarif={
SET sarif="%sarif% \"version\": \"2.1.0\", \"runs\": [{ \"tool\": { \"driver\": { \"name\": \"SpotBugs\" } }, \"results\": ["
SET first=true

REM Parse XML line by line (basic conversion)
FOR /F "usebackq tokens=*" %%A IN ("%input%") DO (
    SET line=%%A
    REM Only process BugInstance entries
    echo !line! | find "<BugInstance" >nul
    IF !ERRORLEVEL! EQU 0 (
        REM Extract type, category, severity, message (basic)
        SET type=
        SET category=
        SET severity=
        SET message=
        FOR %%B IN (!line!) DO (
            echo %%B | find "type=" >nul && SET type=%%~B
            echo %%B | find "category=" >nul && SET category=%%~B
            echo %%B | find "priority=" >nul && SET severity=%%~B
        )
        REM Extract message from next line
        REM Note: Basic approach; for complex XML, use a proper XML parser
        SET message="SpotBugs issue"
        REM Add comma if not first
        IF !first! EQU true (
            SET first=false
        ) ELSE (
            SET sarif=!sarif!, 
        )
        REM Append result
        SET sarif=!sarif!{ "ruleId": "!type!", "level": "warning", "message": { "text": !message! } }
    )
)

SET sarif=!sarif!] }]}

REM Write output
echo !sarif! > "%output%"
echo SARIF report generated: %output%
