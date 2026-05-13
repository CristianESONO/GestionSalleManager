@echo off
setlocal EnableDelayedExpansion

REM Trouver le repertoire du projet (contenant .mvn)
set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
set "MAVEN_PROJECTBASEDIR=%SCRIPT_DIR%"
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn" (
  echo Erreur: repertoire .mvn introuvable. Lancez ce script depuis la racine du projet.
  exit /b 1
)

REM Lire l'URL de distribution
set "WRAPPER_PROP=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
if not exist "%WRAPPER_PROP%" (
  echo Erreur: fichier maven-wrapper.properties introuvable.
  exit /b 1
)
set "DISTRIBUTION_URL="
for /f "usebackq tokens=1,* delims==" %%a in ("%WRAPPER_PROP%") do (
  if "%%a"=="distributionUrl" set "DISTRIBUTION_URL=%%b"
)
if "!DISTRIBUTION_URL!"=="" (
  echo Erreur: distributionUrl non trouve dans maven-wrapper.properties
  exit /b 1
)
set "DISTRIBUTION_URL=!DISTRIBUTION_URL: =!"

REM Lire le nom du dossier Maven dans le zip (sinon valeur par defaut)
set "MAVEN_DIRNAME=apache-maven-3.9.6"
for /f "usebackq tokens=1,* delims==" %%a in ("%WRAPPER_PROP%") do (
  if "%%a"=="distributionDirName" set "MAVEN_DIRNAME=%%b"
)
set "MAVEN_DIRNAME=!MAVEN_DIRNAME: =!"

REM Repertoire local ou stocker Maven (dans le projet)
set "MAVEN_HOME_DIR=%MAVEN_PROJECTBASEDIR%\.mvn\maven"
set "MAVEN_ZIP=%MAVEN_PROJECTBASEDIR%\.mvn\maven.zip"

if not exist "%MAVEN_HOME_DIR%\%MAVEN_DIRNAME%\bin\mvn.cmd" (
  echo Maven non trouve. Telechargement depuis !DISTRIBUTION_URL! ...
  if not exist "%MAVEN_PROJECTBASEDIR%\.mvn" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn"

  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "try { " ^
    "  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
    "  Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_ZIP%' -UseBasicParsing; " ^
    "} catch { Write-Error $_.Exception.Message; exit 1 }"

  if errorlevel 1 (
    echo Echec du telechargement de Maven.
    exit /b 1
  )

  echo Extraction de Maven...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_HOME_DIR%' -Force"

  if not exist "%MAVEN_HOME_DIR%\%MAVEN_DIRNAME%\bin\mvn.cmd" (
    echo Structure inattendue dans le zip. Verifiez .mvn\maven
    exit /b 1
  )

  del /q "%MAVEN_ZIP%" 2>nul
  echo Maven installe dans .mvn\maven\%MAVEN_DIRNAME%
)

set "MAVEN_HOME=%MAVEN_HOME_DIR%\%MAVEN_DIRNAME%"

REM Verifier JAVA_HOME
if "%JAVA_HOME%"=="" (
  set "JAVA_EXE=java"
) else (
  set "JAVA_EXE=%JAVA_HOME%\bin\java"
)
"%JAVA_EXE%" -version 2>nul
if errorlevel 1 (
  echo JAVA_HOME n'est pas defini ou Java est introuvable. Installez JDK 21 et definissez JAVA_HOME.
  exit /b 1
)

REM Lancer Maven
"%MAVEN_HOME%\bin\mvn.cmd" %*
