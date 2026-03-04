; Script Inno Setup pour Gestion Salle Jeu
; Corrections : chemin AppData (Roaming), Run en double supprimé

#define MyAppName "Gestion Salle Jeu"
#define MyAppVersion "1.3.4"
#define MyAppPublisher "Ruslan Cristian ESONO MAYE"
#define MyAppExeName "GestionSalle.exe"
#define MyAppAssocName "Gestion Salle"
#define MyAppAssocExt ".myp"
#define MyAppAssocKey StringChange(MyAppAssocName, " ", "") + MyAppAssocExt

; Dossier où l'app stocke config + base (APPDATA = Roaming, comme dans JpaUtil/AppConfig)
; NE PAS utiliser "localappdata" en texte : utiliser la constante Inno {userappdata}
#define AppDataFolder "{userappdata}\GestionSalles"

[Setup]
AppId={{922C43DF-A497-4585-B362-C5FC43174C79}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\GestionSalles
ArchitecturesInstallIn64BitMode=x64
UninstallDisplayIcon={app}\{#MyAppExeName}
ChangesAssociations=yes
DisableProgramGroupPage=yes
OutputDir=C:\Users\HP\Desktop\Setup
OutputBaseFilename=GestionSalle_Setup_{#MyAppVersion}
SetupIconFile=C:\Users\HP\Desktop\GestionSalle\demo\installer\kayplay-logo.ico
SolidCompression=yes
WizardStyle=modern
AppMutex=GestionSalleMutex

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
; Crée le dossier pour config + base dans AppData Roaming (où l'app cherche)
Name: "{userappdata}\GestionSalles"; Permissions: users-full

[Files]
; EXE principal (Launch4j avec JRE relatif "JRE")
Source: "C:\Users\HP\Desktop\GestionSalleExe\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion

; JAR : obligatoire a cote de l'exe pour que Launch4j puisse lancer l'app
Source: "C:\Users\HP\Desktop\GestionSalleExe\*.jar"; DestDir: "{app}"; Flags: ignoreversion

; JRE local : tout le contenu du dossier JRE vers {app}\JRE
Source: "C:\Users\HP\Desktop\GestionSalleExe\JRE\*"; DestDir: "{app}\JRE"; Flags: ignoreversion recursesubdirs createallsubdirs

; Base SQLite dans le meme dossier que l'app attend (APPDATA\GestionSalles)
Source: "C:\Users\HP\Desktop\GestionSalleExe\gestionsalles.sqlite"; DestDir: "{userappdata}\GestionSalles"; Flags: ignoreversion onlyifdoesntexist

; Script de diagnostic : lancer sur la machine ou l'exe ne s'ouvre pas pour voir l'erreur
Source: "Lancer_Debug.bat"; DestDir: "{app}"; Flags: ignoreversion

[Registry]
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocExt}\OpenWithProgids"; ValueType: string; ValueName: "{#MyAppAssocKey}"; ValueData: ""; Flags: uninsdeletevalue
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}"; ValueType: string; ValueName: ""; ValueData: "{#MyAppAssocName}"; Flags: uninsdeletekey
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\{#MyAppExeName},0"
Root: HKA; Subkey: "Software\Classes\{#MyAppAssocKey}\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; Une seule entrée : lancer l'app après installation
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
const
  ERROR_ALREADY_EXISTS = 183;

function CreateMutex(lpMutexAttributes: LongInt; bInitialOwner: Boolean; 
  lpName: string): LongInt;
  external 'CreateMutexA@kernel32.dll stdcall';

function GetLastError(): LongInt;
  external 'GetLastError@kernel32.dll stdcall';

function ReleaseMutex(hMutex: LongInt): Boolean;
  external 'ReleaseMutex@kernel32.dll stdcall';

function CloseHandle(hObject: LongInt): Boolean;
  external 'CloseHandle@kernel32.dll stdcall';

var
  hMutex: LongInt;

procedure InitializeWizard();
begin
  hMutex := CreateMutex(0, False, 'Global\GestionSalleMutex');
  
  if (hMutex = 0) or (GetLastError() = ERROR_ALREADY_EXISTS) then
  begin
    MsgBox('Une instance de Gestion Salle Jeu est déjà en cours d''exécution.' + #13#10 +
           'Veuillez la fermer avant de continuer.', mbError, MB_OK);
    WizardForm.Close();
  end;
end;

procedure DeinitializeSetup();
begin
  if hMutex <> 0 then
  begin
    ReleaseMutex(hMutex);
    CloseHandle(hMutex);
  end;
end;
