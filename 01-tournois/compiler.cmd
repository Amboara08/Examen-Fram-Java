@echo off
cd /d "%~dp0"
call mvn -s "%~dp0.mvn\settings.xml" -Dmaven.repo.local="%~dp0..\..\.maven-repository" clean verify
pause
