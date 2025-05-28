@echo off
cd /d %~dp0

echo Compilation des fichiers Java...

:: Compile tous les fichiers Java du dossier courant
javac *.java

if errorlevel 1 (
    echo Erreur de compilation.
    exit /b 1
)

echo.
echo Lancement du programme...

java Main

pause