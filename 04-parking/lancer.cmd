@echo off
cd /d "%~dp0"
if exist "target\04-parking-v2-2.0.0.jar" (
 java -jar "target\04-parking-v2-2.0.0.jar"
) else (
 call mvn -s "%~dp0.mvn\settings.xml" -Dmaven.repo.local="%~dp0..\..\.maven-repository" spring-boot:run
)
pause
