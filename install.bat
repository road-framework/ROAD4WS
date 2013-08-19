@echo off

echo Installing ROAD4WS 1.0...
echo --------------------------        
echo Following settings were detected. 
echo AXIS2_HOME = "%AXIS2_HOME%"
echo CATALINA_HOME = "%CATALINA_HOME%"
echo If correct, 
pause


IF "%AXIS2_HOME%" == "" GOTO NO_AXIS2_HOME

:OK
xcopy .\build\*.jar %AXIS2_HOME%\lib /Y
xcopy .\lib\*.jar %AXIS2_HOME%\lib /Y
xcopy .\build\*.mar %AXIS2_HOME%\modules\ /Y
 
 

echo ROAD4WS install complete. Please read the README.txt now. 
pause
GOTO END

:NO_AXIS2_HOME
echo Please set AXIS2_HOME environmental variable. The value should point to .../WEB_INF/
echo You need to have Apache Tomcat 6.0 or later installed (if not already)
echo You need to have Apache Axis2 1.4.0 or later installed (if not already)

pause
GOTO END

:END