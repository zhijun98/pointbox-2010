;Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "PointBox Console"
!define PRODUCT_VERSION "1.0.1"
!define PRODUCT_PUBLISHER "Eclipse Market Solution LLC"
!define PRODUCT_WEB_SITE "http://www.eclipsemarkets.com"
;!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\j2re-setup.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "NSIS:StartMenuDir"

;for jre
!define JRE_VERSION "1.6"

; MUI 1.67 compatible ------
!include "MUI.nsh"
!include "Sections.nsh"

Var InstallJRE
Var InstallCplus
Var JREPath

; C++ redistribution package

;!macro FindIt In For Result
;Push "${In}"
;Push "${For}"
; Call FindIt
;Pop "${Result}"
;!macroend

;!define FindIt "!insertmacro FindIt"

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "pointbox_setup.exe"
;InstallDir "$PROGRAMFILES\PointBox Office"
;InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

InstType "full"	; Uncomment if you want Installation types

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "License.txt"

; check jre
Page custom CheckInstalledJRE

; Define headers for the 'Java/C++ installation successfully' page
!define MUI_PAGE_HEADER_TEXT "Prerequisite Runtime Environment"
!define MUI_PAGE_HEADER_SUBTEXT "PointBox Office runtime environment requires Java runtime environment (1.6 or later) and Microsoft Visual C++ 2008 Redistribution Package."
!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "Prerequisite runtime environment checking process is completed."
!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "Both of Java runtime environment (1.6 or later) and Microsoft Visual C++ 2008 Redistribution Package are ready for PointBox Office installation."

!insertmacro MUI_PAGE_INSTFILES

!define MUI_PAGE_HEADER_TEXT "${PRODUCT_NAME} Installation"
!define MUI_PAGE_HEADER_SUBTEXT "Launch ${PRODUCT_NAME} installation now ..."
!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "${PRODUCT_NAME} Installation has been completed."
!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "Thank you for choosing  ${PRODUCT_NAME}"

;C++
;Page custom CheckInstalledCplus

;!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "C++ installation complete"
;!define MUI_PAGE_HEADER_TEXT "Installing C++ runtime"
;!define MUI_PAGE_HEADER_SUBTEXT "Please wait while we install the C++ runtime"
;!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "C++ runtime installed successfully."

;!insertmacro MUI_PAGE_INSTFILES


;!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "C++ Installation complete"
;!define MUI_PAGE_HEADER_TEXT "Installing"





InstallDir "$PROGRAMFILES\PointBox Console"

;!define MUI_PAGE_HEADER_SUBTEXT "Please wait while ${PRODUCT_NAME} is being installed."
!define MUI_PAGE_CUSTOMFUNCTION_PRE myPreInstfiles
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE RestoreSections
  
  
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Start menu page
var ICONS_GROUP
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "PointBox Console"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_STARTMENU_REGVAL}"
!insertmacro MUI_PAGE_STARTMENU Application $ICONS_GROUP
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\PointBox.bat"
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; MUI Settings
!define MUI_ABORTWARNING
;!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
;!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Language files
!insertmacro MUI_LANGUAGE "English"

  ;Description
  LangString DESC_SecAppFiles ${LANG_ENGLISH} "Application files copy"


;Header
LangString TEXT_JRE_TITLE ${LANG_ENGLISH} "Java Runtime Environment"
LangString TEXT_JRE_SUBTITLE ${LANG_ENGLISH} "Installation"
LangString TEXT_PRODVER_TITLE ${LANG_ENGLISH} "Installed version of ${PRODUCT_NAME}"
LangString TEXT_PRODVER_SUBTITLE ${LANG_ENGLISH} "Installation cancelled"

ReserveFile "jre.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS


; MUI end ------


Section -installjre jre
  Push $0
  Push $1

;  MessageBox MB_OK "Inside JRE Section"
  Strcmp $InstallJRE "yes" InstallJRE JREPathStorage
  DetailPrint "Starting the JRE installation"
InstallJRE:
  File /oname=$TEMP\jre_setup.exe j2re-setup.exe
  MessageBox MB_OK "PointBox Office requires Java runtime version 1.6 or later. You will be requested to install Java runtime now."
  DetailPrint "Launching JRE setup"
  ;ExecWait "$TEMP\jre_setup.exe /S" $0
  ; The silent install /S does not work for installing the JRE, sun has documentation on the
  ; parameters needed.  I spent about 2 hours hammering my head against the table until it worked
  ExecWait '"$TEMP\jre_setup.exe"' $0
  ;ExecWait '"$TEMP\jre_setup.exe" /s /v\"/qn REBOOT=Suppress JAVAUPDATE=0 WEBSTARTICON=0\"' $0
  DetailPrint "Setup finished"
  Delete "$TEMP\jre_setup.exe"
  StrCmp $0 "0" InstallVerif 0
  Push "The JRE setup has been canceled or abnormally interrupted."
  Goto ExitInstallJRE

InstallVerif:
  DetailPrint "Checking the JRE Setup's outcome"
;  MessageBox MB_OK "Checking JRE outcome"
  Push "${JRE_VERSION}"
  Call DetectJRE
  Pop $0	  ; DetectJRE's return value
  StrCmp $0 "0" ExitInstallJRE 0
  StrCmp $0 "-1" ExitInstallJRE 0
  Goto JavaExeVerif
  Push "The JRE setup failed"
  Goto ExitInstallJRE

JavaExeVerif:
  IfFileExists $0 JREPathStorage 0
  Push "The following file : $0, cannot be found."
  Goto ExitInstallJRE

JREPathStorage:
;  MessageBox MB_OK "Path Storage"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $1
  StrCpy $JREPath $0
  Goto End

ExitInstallJRE:
  Pop $1
  MessageBox MB_OK "The setup is about to quit for the following reason : $1"
  Pop $1 	; Restore $1
  Pop $0 	; Restore $0
  Abort
End:
  Pop $1	; Restore $1
  Pop $0	; Restore $0
SectionEnd




Section -installC cplus
File /oname=$TEMP\vcredist_x86.exe vcredist1_x86.exe
;${FindIt} "C:\" "VC_RED.cab" "$R0"

IfFileExists C:\VC_RED.cab skipInstallCplus InstallCplus

;Strcmp $R0 "-1" InstallCplus End

;MessageBox MB_OK $R0
;Strcmp $R0 "-1" InstallCplus

InstallCplus:
 MessageBox MB_OK "Microsoft Visual C++ 2008 Redistributable was not found to support PB Pricing engine. You are required to install this package if you want to take advantage of PB Pricing engine. It is going to launch this setup now..."
 DetailPrint "Launching Microsoft Visual C++ 2008 Redistributable Setup ..."
 ExecWait '"$TEMP\vcredist_x86.exe"' $0
 ; DetailPrint "Setup finished"
 Delete "$TEMP\vcredist_x86.exe"


skipInstallCplus:

SectionEnd




Section "Installation of ${PRODUCT_NAME}" SecAppFiles
  SetOverwrite try
  SetOutPath "$INSTDIR\lib"
  File "Production\lib\AbsoluteLayout.jar"
  File "Production\lib\activation.jar"
  File "Production\lib\annotations.jar"
  File "Production\lib\asm-all-2.2.2.jar"
  File "Production\lib\bcmail-jdk16-140.jar"
  File "Production\lib\bcprov-jdk16-140.jar"
  File "Production\lib\bctsp-jdk16-140.jar"
  File "Production\lib\bsf.jar"
  File "Production\lib\commons-logging-1.1.1.jar"
  File "Production\lib\commons-logging-adapters-1.1.jar"
  File "Production\lib\commons-logging-api-1.1.jar"
  File "Production\lib\commons-net-ftp-2.0.jar"
  File "Production\lib\daimoscar.jar"
  File "Production\lib\derby.jar"
  File "Production\lib\eclipselink-2.2.0.jar"
  File "Production\lib\eclipselink-javax.persistence-2.0.jar"
  File "Production\lib\fest-assert-1.1.jar"
  File "Production\lib\fest-reflect-1.1.jar"
  File "Production\lib\fest-swing-1.2a3.jar"
  File "Production\lib\fest-util-1.1.jar"
  File "Production\lib\Filters.jar"
  File "Production\lib\flamingo.jar"
  File "Production\lib\forms-1.2.0.jar"
  File "Production\lib\freixas.jar"
  File "Production\lib\jdesktop.jar"
  File "Production\lib\jdom-1.0.jar"
  File "Production\lib\jimi.jar"
  File "Production\lib\jmf.jar"
  File "Production\lib\jsocks-klea.jar"
  File "Production\lib\jspeex-0.9.7-jfcom.jar"
  File "Production\lib\jstun.jar"
  File "Production\lib\junit-4.4.jar"
  File "Production\lib\junit-4.6.jar"
  File "Production\lib\junit.jar"
  File "Production\lib\junit-addons-1.4.jar"
  File "Production\lib\jzlib.jar"
  File "Production\lib\l2fprod-common-7.3.jar"
  File "Production\lib\laf-plugin.jar"
  File "Production\lib\laf-widget.jar"
  File "Production\lib\looks-2.0.1.jar"
  File "Production\lib\mail.jar"
  File "Production\lib\mina-core-1.0.1.jar"
  File "Production\lib\openymsg_0_5_0.jar"
  File "Production\lib\PointBoxCommons.jar"
  File "Production\lib\PointBoxPartner.jar"
  File "Production\lib\PointBoxPricer.jar"
  File "Production\lib\PointBoxRuntime.jar"
  File "Production\lib\PointBoxSupport.jar"
  File "Production\lib\resolver.jar"
  File "Production\lib\serializer.jar"
  File "Production\lib\smack.jar"
  File "Production\lib\smackx.jar"
  File "Production\lib\Speex.jar"
  File "Production\lib\substance_6.jar"
  File "Production\lib\substance-swingx.jar"
  File "Production\lib\swingx.jar"
  File "Production\lib\tigristoolbar.jar"
  File "Production\lib\trident.jar"
  File "Production\lib\xercesImpl.jar"
  File "Production\lib\xercesSamples.jar"
  File "Production\lib\xml-apis.jar"
  File "Production\lib\xpp.jar"
  File "Production\lib\ZComApproachLibrary.jar"
  
  SetOutPath "$INSTDIR"
  File "Production\pbconsole_splash.jpg"
  File "Production\pb_log.ico"
  File "Production\pb_site.ico"
  File "Production\pb_delete.ico"
  File "Production\regular_pricing.dll"
  File "Production\auto_pricing.dll"
  File "Production\sheet_pricer.dll"
  File "Production\PointBox.bat"
  File "Production\PointBoxConsole.jar"

  SetOutPath "$INSTDIR\pricing_runtime"
  File "Production\pricing_runtime\Brokers.imp"
  File "Production\pricing_runtime\Expirations.txt"
  File "Production\pricing_runtime\Holidays.txt"
  File "Production\pricing_runtime\interpSwaptionSkewSurface.imp"
  File "Production\pricing_runtime\interpSwaptionVolSurface.imp"
  File "Production\pricing_runtime\LIBOR.USD.imp"
  File "Production\pricing_runtime\Locations.txt"
  File "Production\pricing_runtime\ModelATMVolABS.imp"
  File "Production\pricing_runtime\NG_NYMEX.imp"
  File "Production\pricing_runtime\volInterpSkewSurface.imp"
  File "Production\pricing_runtime\volInterpSurface.imp"
  
  File "Production\pricing_runtime\CO_BRT_IPE_Fut.imp"
  File "Production\pricing_runtime\CO_BRT_IPE_Swap.imp"
  File "Production\pricing_runtime\CO_contractIR.imp"
  File "Production\pricing_runtime\CO_expirations_brt.txt"
  File "Production\pricing_runtime\CO_expirations_wti.txt"
  File "Production\pricing_runtime\CO_holidays_ipe.txt"
  File "Production\pricing_runtime\CO_holidays_ldn.txt"
  File "Production\pricing_runtime\CO_holidays_ny.txt"
  File "Production\pricing_runtime\CO_holidays_nymex.txt"
  File "Production\pricing_runtime\CO_interpSwaptionSkewSurface.imp"
  File "Production\pricing_runtime\CO_LIBOR.USD.imp"
  File "Production\pricing_runtime\CO_Locations.txt"
  File "Production\pricing_runtime\CO_ModelATMVolABSBRT.imp"
  File "Production\pricing_runtime\CO_ModelATMVolABSBRTA.imp"
  File "Production\pricing_runtime\CO_ModelATMVolABSWTI.imp"
  File "Production\pricing_runtime\CO_ModelATMVolABSWTIA.imp"
  File "Production\pricing_runtime\CO_VolSkewSurfaceBRT.imp"
  File "Production\pricing_runtime\CO_VolSkewSurfaceBRTA.imp"
  File "Production\pricing_runtime\CO_VolSkewSurfaceWTI.imp"
  File "Production\pricing_runtime\CO_VolSkewSurfaceWTIA.imp"
  File "Production\pricing_runtime\CO_WTI_NYMEX_Fut.imp"
  File "Production\pricing_runtime\CO_WTI_NYMEX_Swap.imp"

  File "Production\pricing_runtime\NG_contractIR.imp"
  File "Production\pricing_runtime\NG_Expirations.txt"
  File "Production\pricing_runtime\NG_holidays_ldn.txt"
  File "Production\pricing_runtime\NG_holidays_ny.txt"
  File "Production\pricing_runtime\NG_holidays_nymex.txt"
  File "Production\pricing_runtime\NG_interpSwaptionSkewSurface.imp"
  File "Production\pricing_runtime\NG_LIBOR.USD.imp"
  File "Production\pricing_runtime\NG_Locations.txt"
  File "Production\pricing_runtime\NG_ModelATMVolABS.imp"
  File "Production\pricing_runtime\NG_volInterpSkewSurface.imp"

  File "Production\pricing_runtime\PWR_contractIR.imp"
  File "Production\pricing_runtime\PWR_Expirations.txt"
  File "Production\pricing_runtime\PWR_holidays_ldn.txt"
  File "Production\pricing_runtime\PWR_holidays_ny.txt"
  File "Production\pricing_runtime\PWR_holidays_nymex.txt"
  File "Production\pricing_runtime\PWR_interpSwaptionSkewSurface.imp"
  File "Production\pricing_runtime\PWR_LIBOR.USD.imp"
  File "Production\pricing_runtime\PWR_Locations.txt"
  File "Production\pricing_runtime\PWR_ModelATMVolABS.imp"
  File "Production\pricing_runtime\PWR_NYMEX.imp"
  File "Production\pricing_runtime\PWR_volInterpSkewSurface.imp"
  
  SetOutPath "$INSTDIR"
  SetOverwrite ifnewer
  File "Production\readme.txt"

; Shortcuts
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\PointBox Console.lnk" "$INSTDIR\PointBox.bat" "" "$INSTDIR\pb_log.ico"
  CreateShortCut "$DESKTOP\PointBox Console.lnk" "$INSTDIR\PointBox.bat" "" "$INSTDIR\pb_log.ico"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd



;Section "ZZJCSecition" SEC03
 ; File "..\..\Downloads\vcredist_x86.exe"

; Shortcuts
  ;!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  ;!insertmacro MUI_STARTMENU_WRITE_END
;SectionEnd

Section "Start menu shortcuts" SecCreateShortcut
  SetOutPath $INSTDIR
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\PointBox Site.lnk" "$INSTDIR\${PRODUCT_NAME}.url" "" "$INSTDIR\pb_site.ico"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall PointBox.lnk" "$INSTDIR\uninst.exe" "" "$INSTDIR\pb_delete.ico"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section -Post
 WriteUninstaller "$INSTDIR\uninst.exe"
 ; WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$TEMP\j2re-setup.exe"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^nAME)"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$TEMP\j2re-setup.exe"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
 ; WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  !insertmacro MUI_STARTMENU_GETFOLDER "Application" $ICONS_GROUP
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$TEMP\vcredist_x86.exe"
  Delete "$TEMP\j2re-setup.exe"
  Delete "$INSTDIR\readme.txt"
  Delete "$INSTDIR\pricing_runtime\volInterpSurface.imp"
  Delete "$INSTDIR\pricing_runtime\volInterpSkewSurface.imp"
  Delete "$INSTDIR\pricing_runtime\NG_NYMEX.imp"
  Delete "$INSTDIR\pricing_runtime\ModelATMVolABS.imp"
  Delete "$INSTDIR\pricing_runtime\Locations.txt"
  Delete "$INSTDIR\pricing_runtime\LIBOR.USD.imp"
  Delete "$INSTDIR\pricing_runtime\interpSwaptionVolSurface.imp"
  Delete "$INSTDIR\pricing_runtime\interpSwaptionSkewSurface.imp"
  Delete "$INSTDIR\pricing_runtime\Holidays.txt"
  Delete "$INSTDIR\pricing_runtime\Expirations.txt"
  Delete "$INSTDIR\pricing_runtime\Brokers.imp"

  Delete "$INSTDIR\pricing_runtime\CO_BRT_IPE_Fut.imp"
  Delete "$INSTDIR\pricing_runtime\CO_BRT_IPE_Swap.imp"
  Delete "$INSTDIR\pricing_runtime\CO_contractIR.imp"
  Delete "$INSTDIR\pricing_runtime\CO_expirations_brt.txt"
  Delete "$INSTDIR\pricing_runtime\CO_expirations_wti.txt"
  Delete "$INSTDIR\pricing_runtime\CO_holidays_ipe.txt"
  Delete "$INSTDIR\pricing_runtime\CO_holidays_ldn.txt"
  Delete "$INSTDIR\pricing_runtime\CO_holidays_ny.txt"
  Delete "$INSTDIR\pricing_runtime\CO_holidays_nymex.txt"
  Delete "$INSTDIR\pricing_runtime\CO_interpSwaptionSkewSurface.imp"
  Delete "$INSTDIR\pricing_runtime\CO_LIBOR.USD.imp"
  Delete "$INSTDIR\pricing_runtime\CO_Locations.txt"
  Delete "$INSTDIR\pricing_runtime\CO_ModelATMVolABSBRT.imp"
  Delete "$INSTDIR\pricing_runtime\CO_ModelATMVolABSBRTA.imp"
  Delete "$INSTDIR\pricing_runtime\CO_ModelATMVolABSWTI.imp"
  Delete "$INSTDIR\pricing_runtime\CO_ModelATMVolABSWTIA.imp"
  Delete "$INSTDIR\pricing_runtime\CO_VolSkewSurfaceBRT.imp"
  Delete "$INSTDIR\pricing_runtime\CO_VolSkewSurfaceBRTA.imp"
  Delete "$INSTDIR\pricing_runtime\CO_VolSkewSurfaceWTI.imp"
  Delete "$INSTDIR\pricing_runtime\CO_VolSkewSurfaceWTIA.imp"
  Delete "$INSTDIR\pricing_runtime\CO_WTI_NYMEX_Fut.imp"
  Delete "$INSTDIR\pricing_runtime\CO_WTI_NYMEX_Swap.imp"

  Delete "$INSTDIR\pricing_runtime\NG_contractIR.imp"
  Delete "$INSTDIR\pricing_runtime\NG_Expirations.txt"
  Delete "$INSTDIR\pricing_runtime\NG_holidays_ldn.txt"
  Delete "$INSTDIR\pricing_runtime\NG_holidays_ny.txt"
  Delete "$INSTDIR\pricing_runtime\NG_holidays_nymex.txt"
  Delete "$INSTDIR\pricing_runtime\NG_interpSwaptionSkewSurface.imp"
  Delete "$INSTDIR\pricing_runtime\NG_LIBOR.USD.imp"
  Delete "$INSTDIR\pricing_runtime\NG_Locations.txt"
  Delete "$INSTDIR\pricing_runtime\NG_ModelATMVolABS.imp"
  Delete "$INSTDIR\pricing_runtime\NG_volInterpSkewSurface.imp"

  Delete "$INSTDIR\pricing_runtime\PR_contractIR.imp"
  Delete "$INSTDIR\pricing_runtime\PR_Expirations.txt"
  Delete "$INSTDIR\pricing_runtime\PR_holidays_ldn.txt"
  Delete "$INSTDIR\pricing_runtime\PR_holidays_ny.txt"
  Delete "$INSTDIR\pricing_runtime\PR_holidays_nymex.txt"
  Delete "$INSTDIR\pricing_runtime\PR_interpSwaptionSkewSurface.imp"
  Delete "$INSTDIR\pricing_runtime\PR_LIBOR.USD.imp"
  Delete "$INSTDIR\pricing_runtime\PR_Locations.txt"
  Delete "$INSTDIR\pricing_runtime\PR_ModelATMVolABS.imp"
  Delete "$INSTDIR\pricing_runtime\PR_NYMEX.imp"
  Delete "$INSTDIR\pricing_runtime\PR_volInterpSkewSurface.imp"

  Delete "$INSTDIR\pboffice.bat"
  Delete "$INSTDIR\regular_pricing.dll"
  Delete "$INSTDIR\auto_pricing.dll"
  Delete "$INSTDIR\sheet_pricer.dll"
  Delete "$INSTDIR\pb_delete.ico"
  Delete "$INSTDIR\pb_site.ico"
  Delete "$INSTDIR\pb_log.ico"
  Delete "$INSTDIR\pbconsole_splash.jpg"


  Delete "$INSTDIR\lib\AbsoluteLayout.jar"
  Delete "$INSTDIR\lib\activation.jar"
  Delete "$INSTDIR\lib\annotations.jar"
  Delete "$INSTDIR\lib\asm-all-2.2.2.jar"
  Delete "$INSTDIR\lib\bcmail-jdk16-140.jar"
  Delete "$INSTDIR\lib\bcprov-jdk16-140.jar"
  Delete "$INSTDIR\lib\bctsp-jdk16-140.jar"
  Delete "$INSTDIR\lib\bsf.jar"
  Delete "$INSTDIR\lib\commons-logging-1.1.1.jar"
  Delete "$INSTDIR\lib\commons-logging-adapters-1.1.jar"
  Delete "$INSTDIR\lib\commons-logging-api-1.1.jar"
  Delete "$INSTDIR\lib\commons-net-ftp-2.0.jar"
  Delete "$INSTDIR\lib\daimoscar.jar"
  Delete "$INSTDIR\lib\derby.jar"
  Delete "$INSTDIR\lib\eclipselink-2.2.0.jar"
  Delete "$INSTDIR\lib\eclipselink-javax.persistence-2.0.jar"
  Delete "$INSTDIR\lib\fest-assert-1.1.jar"
  Delete "$INSTDIR\lib\fest-reflect-1.1.jar"
  Delete "$INSTDIR\lib\fest-swing-1.2a3.jar"
  Delete "$INSTDIR\lib\fest-util-1.1.jar"
  Delete "$INSTDIR\lib\Filters.jar"
  Delete "$INSTDIR\lib\flamingo.jar"
  Delete "$INSTDIR\lib\forms-1.2.0.jar"
  Delete "$INSTDIR\lib\freixas.jar"
  Delete "$INSTDIR\lib\jdesktop.jar"
  Delete "$INSTDIR\lib\jdom-1.0.jar"
  Delete "$INSTDIR\lib\jimi.jar"
  Delete "$INSTDIR\lib\jmf.jar"
  Delete "$INSTDIR\lib\jsocks-klea.jar"
  Delete "$INSTDIR\lib\jspeex-0.9.7-jfcom.jar"
  Delete "$INSTDIR\lib\jstun.jar"
  Delete "$INSTDIR\lib\junit-4.4.jar"
  Delete "$INSTDIR\lib\junit-4.6.jar"
  Delete "$INSTDIR\lib\junit.jar"
  Delete "$INSTDIR\lib\junit-addons-1.4.jar"
  Delete "$INSTDIR\lib\jzlib.jar"
  Delete "$INSTDIR\lib\l2fprod-common-7.3.jar"
  Delete "$INSTDIR\lib\laf-plugin.jar"
  Delete "$INSTDIR\lib\laf-widget.jar"
  Delete "$INSTDIR\lib\looks-2.0.1.jar"
  Delete "$INSTDIR\lib\mail.jar"
  Delete "$INSTDIR\lib\mina-core-1.0.1.jar"
  Delete "$INSTDIR\lib\openymsg_0_5_0.jar"
  Delete "$INSTDIR\lib\PointBoxCommons.jar"
  Delete "$INSTDIR\lib\PointBoxPartner.jar"
  Delete "$INSTDIR\lib\PointBoxPricer.jar"
  Delete "$INSTDIR\lib\PointBoxRuntime.jar"
  Delete "$INSTDIR\lib\PointBoxSupport.jar"
  Delete "$INSTDIR\lib\resolver.jar"
  Delete "$INSTDIR\lib\serializer.jar"
  Delete "$INSTDIR\lib\smack.jar"
  Delete "$INSTDIR\lib\smackx.jar"
  Delete "$INSTDIR\lib\Speex.jar"
  Delete "$INSTDIR\lib\substance_6.jar"
  Delete "$INSTDIR\lib\substance-swingx.jar"
  Delete "$INSTDIR\lib\swingx.jar"
  Delete "$INSTDIR\lib\tigristoolbar.jar"
  Delete "$INSTDIR\lib\trident.jar"
  Delete "$INSTDIR\lib\xercesImpl.jar"
  Delete "$INSTDIR\lib\xercesSamples.jar"
  Delete "$INSTDIR\lib\xml-apis.jar"
  Delete "$INSTDIR\lib\xpp.jar"
  Delete "$INSTDIR\lib\ZComApproachLibrary.jar"

  Delete "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Website.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\PointBox Console.lnk"
  Delete "PointBox Console.lnk"

  Delete "$DESKTOP\PointBox Console.lnk"
  
  RMDir "$SMPROGRAMS\$ICONS_GROUP"
  RMDir "$INSTDIR\pricing_runtime"
  RMDir "$INSTDIR\lib"
  RMDir "$INSTDIR"
  RMDir ""

  ;;DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  ;;DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  SetAutoClose true
SectionEnd


;functions for JRE

;--------------------------------
;Installer Functions

Function .onInit

  ;Extract InstallOptions INI Files
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "jre.ini"
  Call SetupSections

FunctionEnd

Function myPreInstfiles

  Call RestoreSections
  SetAutoClose true

FunctionEnd

Function CheckInstalledJRE
  ;MessageBox MB_OK "PointBox Office requires Java runtime version 1.6 or later."
  Push "${JRE_VERSION}"
  Call DetectJRE
  ;Messagebox MB_OK "Done checking JRE version"
  Exch $0	; Get return value from stack
  StrCmp $0 "0" NoFound
  StrCmp $0 "-1" FoundOld
  Goto JREAlreadyInstalled

FoundOld:
  ;MessageBox MB_OK "Old JRE found"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "${PRODUCT_NAME} requires a more recent version of the Java Runtime Environment than the one found on your computer. The installation of JRE ${JRE_VERSION} will start."
  !insertmacro MUI_HEADER_TEXT "$(TEXT_JRE_TITLE)" "$(TEXT_JRE_SUBTITLE)"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE

NoFound:
  ;MessageBox MB_OK "JRE not found"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "Field 1" "Text" "No Java Runtime Environment could be found on your computer. The installation of JRE v${JRE_VERSION} will start."
  !insertmacro MUI_HEADER_TEXT "$(TEXT_JRE_TITLE)" "$(TEXT_JRE_SUBTITLE)"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "jre.ini"
  Goto MustInstallJRE

MustInstallJRE:
  Exch $0	; $0 now has the installoptions page return value
  ; Do something with return value here
  Pop $0	; Restore $0
  StrCpy $InstallJRE "yes"
  Return

JREAlreadyInstalled:
;  MessageBox MB_OK "No download: ${TEMP2}"
  ;MessageBox MB_OK "JRE already installed"
  StrCpy $InstallJRE "no"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jre.ini" "UserDefinedSection" "JREPath" $JREPATH
  Pop $0		; Restore $0
  Return

FunctionEnd

; Returns: 0 - JRE not found. -1 - JRE found but too old. Otherwise - Path to JAVA EXE

; DetectJRE. Version requested is on the stack.
; Returns (on stack)	"0" on failure (java too old or not installed), otherwise path to java interpreter
; Stack value will be overwritten!

Function DetectJRE
  Exch $0	; Get version requested
		; Now the previous value of $0 is on the stack, and the asked for version of JDK is in $0
  Push $1	; $1 = Java version string (ie 1.5.0)
  Push $2	; $2 = Javahome
  Push $3	; $3 and $4 are used for checking the major/minor version of java
  Push $4
;  MessageBox MB_OK "Detecting JRE"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
;  MessageBox MB_OK "Read : $1"
  StrCmp $1 "" DetectTry2
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "JavaHome"
;  MessageBox MB_OK "Read 3: $2"
  StrCmp $2 "" DetectTry2
  Goto GetJRE

DetectTry2:
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
;  MessageBox MB_OK "Detect Read : $1"
  StrCmp $1 "" NoFound
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$1" "JavaHome"
;  MessageBox MB_OK "Detect Read 3: $2"
  StrCmp $2 "" NoFound

GetJRE:
; $0 = version requested. $1 = version found. $2 = javaHome
;  MessageBox MB_OK "Getting JRE"
  IfFileExists "$2\bin\java.exe" 0 NoFound
  StrCpy $3 $0 1			; Get major version. Example: $1 = 1.5.0, now $3 = 1
  StrCpy $4 $1 1			; $3 = major version requested, $4 = major version found
;  MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 0 FoundOld FoundNew
  StrCpy $3 $0 1 2
  StrCpy $4 $1 1 2			; Same as above. $3 is minor version requested, $4 is minor version installed
;  MessageBox MB_OK "Want $3 , found $4"
  IntCmp $4 $3 FoundNew FoundOld FoundNew

NoFound:
;  MessageBox MB_OK "JRE not found"
  Push "0"
  Goto DetectJREEnd

FoundOld:
;  MessageBox MB_OK "JRE too old: $3 is older than $4"
;  Push ${TEMP2}
  Push "-1"
  Goto DetectJREEnd
FoundNew:
;  MessageBox MB_OK "JRE is new: $3 is newer than $4"

  Push "$2\bin\java.exe"
;  Push "OK"
;  Return
   Goto DetectJREEnd
DetectJREEnd:
	; Top of stack is return value, then r4,r3,r2,r1
	Exch	; => r4,rv,r3,r2,r1,r0
	Pop $4	; => rv,r3,r2,r1r,r0
	Exch	; => r3,rv,r2,r1,r0
	Pop $3	; => rv,r2,r1,r0
	Exch 	; => r2,rv,r1,r0
	Pop $2	; => rv,r1,r0
	Exch	; => r1,rv,r0
	Pop $1	; => rv,r0
	Exch	; => r0,rv
	Pop $0	; => rv
FunctionEnd

Function RestoreSections
  !insertmacro UnselectSection ${jre}
  !insertmacro UnselectSection ${cplus}
  !insertmacro SelectSection ${SecAppFiles}
  !insertmacro SelectSection ${SecCreateShortcut}

FunctionEnd

Function SetupSections
  !insertmacro SelectSection ${jre}
  !insertmacro SelectSection ${cplus}
  !insertmacro UnselectSection ${SecAppFiles}
  !insertmacro UnselectSection ${SecCreateShortcut}
FunctionEnd



;;C++

;Function CheckInstalledCplus
;MessageBox MB_OK "FindIt just start ..."
;${FindIt} "C:\" "VC_RED.cab" "$InstallCplus"
;MessageBox MB_OK "FindIt completed"
;FunctionEnd


;Function FindIt
;IfFileExists C:\autoexec.bat found notFound
;found:

;notFound:

;FunctionEnd
