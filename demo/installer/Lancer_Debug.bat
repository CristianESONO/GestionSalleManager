@echo off
chcp 65001 >nul
title Diagnostic Gestion Salle
cd /d "%~dp0"

echo ============================================
echo   Diagnostic Gestion Salle
echo ============================================
echo.
echo Dossier : %CD%
echo.

echo [1] Test du JRE...
if not exist "JRE\bin\java.exe" (
    echo ERREUR : JRE\bin\java.exe introuvable.
    echo Assurez-vous que le dossier JRE est bien a cote de ce fichier.
    goto :fin
)
"JRE\bin\java.exe" -version
if errorlevel 1 (
    echo ERREUR : le JRE ne s execute pas correctement.
    goto :fin
)
echo OK.
echo.

echo [2] Recherche du fichier JAR...
set JAR=
for %%a in (*.jar) do set "JAR=%%a"
if not defined JAR (
    echo ERREUR : aucun fichier .jar trouve dans ce dossier.
    echo L executable a besoin du fichier JAR a cote de lui.
    goto :fin
)
echo JAR trouve : %JAR%
echo.

echo [3] Lancement de l application (message d erreur ci-dessous si echec)...
echo --------------------------------------------
"JRE\bin\java.exe" -jar "%JAR%"
echo --------------------------------------------
echo.
echo Code de sortie : %errorlevel%

:fin
echo.
pause
