@echo off

set SCRIPT_DIR=%~dp0

java -Djava.util.logging.config.file=%SCRIPT_DIR%/bookstore.properties -jar %SCRIPT_DIR%/launcher.jar