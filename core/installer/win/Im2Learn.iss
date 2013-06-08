; Setup script for use with Inno Setup
; http://www.jrsoftware.org/isinfo.php

[Setup]
AppName=Im2Learn
AppVerName=Im2Learn 2.0
AppPublisher=NCSA
AppPublisherURL=http://isda.ncsa.uiuc.edu
AppSupportURL=http://isda.ncsa.uiuc.edu
AppUpdatesURL=http://isda.ncsa.uiuc.edu
DefaultDirName={pf}\NCSA\Im2Learn
DefaultGroupName=Im2Learn
LicenseFile=@BUILD@\win\license.txt
Compression=lzma
SolidCompression=yes
OutputDir=@BUILD@

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "@BUILD@\win\Im2Learn.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "@BUILD@\win\ext\*"; DestDir: "{app}\ext"; Flags: ignoreversion recursesubdirs
Source: "@BUILD@\win\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs
Source: "@BUILD@\win\license.txt"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[INI]
Filename: "{app}\Im2Learn.url"; Section: "InternetShortcut"; Key: "URL"; String: "http://isda.ncsa.uiuc.edu"

[Icons]
Name: "{group}\Im2Learn"; Filename: "{app}\Im2Learn.exe"
Name: "{group}\{cm:ProgramOnTheWeb,Im2Learn}"; Filename: "{app}\Im2Learn.url"
Name: "{group}\{cm:UninstallProgram,Im2Learn}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\Im2Learn"; Filename: "{app}\Im2Learn.exe"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\Im2Learn"; Filename: "{app}\Im2Learn.exe"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\Im2Learn.exe"; Description: "{cm:LaunchProgram,Im2Learn}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\Im2Learn.url"
Type: files; Name: "{app}\Im2Learn.log"

