@echo off
setlocal
cd /d "%~dp0"
echo Starting Neptune Cash Flow Analyzer at http://localhost:8081/
call mvn spring-boot:run -Dspring-boot.run.profiles=dev,cashflow-test,cashflow-standalone
endlocal
