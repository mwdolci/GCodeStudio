@echo off
echo Compilation des fichiers Java...

:: Compiler tous les fichiers dans le dossier src
javac Front-GCodeStudio\src\*.java

if errorlevel 1 (
    echo Erreur de compilation.
    exit /b 1
)

echo.
echo Lancement du programme...

:: Exécuter la classe main 
java -cp Front-GCodeStudio\src Main

pause