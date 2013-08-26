Following document will describe how to install ROAD4WS

Pre-requisites:
--------------------------------------------------------

 1. Install Apache Tomcat 6.0 or later (http://tomcat.apache.org/download-60.cgi)

 2. Install Apache Axis2 1.6.0 or later (http://ws.apache.org/axis2/)

Environment varaibles:
--------------------------------------------------------

 1. AXIS2_HOME - to the WEB-INF directory  
e.g. AXIS2_HOME= E:\software\apache-tomcat-7.0.8\webapps\axis2\WEB-INF\

 2. CATALINA_HOME  to the tomcat installation  
e.g. CATALINA_HOME = E:\software\apache-tomcat-7.0.8\

ROAD4WS Installation Steps:
--------------------------------------------------------

 * Add the following snippet to the %AXIS2_HOME%/conf/axis2.xml under XML element <axisconfig name="AxisJava2.0">

```  
<deployer extension=".xml" directory="road_composites"  
          class="au.swin.ict.research.cs3.road.road4ws.core.deployer.DefaultROADDeployer"/>
```
 * Run the install.bat. 

How to start the server?
--------------------------------------------------------
Start the Tomcat using script (TOMCAT_HOME/bin/startup.bat)

How to deploy a ROAD Composite?
--------------------------------------------------------
 1. Create road_composites directory under AXIS2_HOME/ if not already  
 2. Copy a composite descriptor(e.g. RoSaS.xml took from ROADfactory) to the road_composites directory. (Alternatively, use the ant scripts available in build.xml)  
 3. Make sure the directory in which the rules(*.drl) are placed are specified correctly in the descriptor. Also copy any dependent jars as instructed by ROADfactory README.txt. Default= TOMCAT_HOME/bin/data/rules
 4. Open web browser and type http://localhost:8080/axis2/  
 5. Click on services to see the WSDLs of deployed services.

Note: You do not need to restart the server to deploy composites

Contact
--------------------------------------------------------
 * Malinda Kapuruge (mkapuruge at swin dot edu dot au)
 
Acknowledgement
--------------------------------------------------------
This project has been researched & developed by Swinburne University partly with the support of Smart Services CRC. To find out more about Smart Services please visit www.smartservicescrc.com.au.
