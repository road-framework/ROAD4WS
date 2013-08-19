#!/bin/sh
echo "Welcome! Installing ROAD4WS 1.0..."
echo "You need to have Apache Tomcat 6.0 or later installed" 
echo "You need to have Apache Axis2 1.4.0 or later installed" 

if [ -z "$AXIS2_HOME" ]; then
    echo "Please set AXIS2_HOME environmental variable. The value should point to .../WEB_INF/"
    exit 1
fi  

cp build/*.jar $AXIS2_HOME/lib/
cp lib/*.jar $AXIS2_HOME/lib/
cp build/*.rmar $AXIS2_HOME/modules/
 
echo "ROAD4WS install complete. Please start the server." 
