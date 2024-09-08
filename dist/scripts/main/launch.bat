@echo off
setlocal

REM Make sure all the necessary certificates are trusted by the system.
CALL %~dp0load_trusted_certs.bat

REM Start the node
java -Xmx2g ^
    -Dlogback.configurationFile=./config/logback.xml ^
    -cp "lib/*;userclasses;userlib/*" ^
    -Djava.system.class.loader="org.sensorhub.utils.NativeClassLoader" ^
    -Djavax.net.ssl.keyStore="./config/osh-keystore.p12" ^
    -Djavax.net.ssl.keyStorePassword="osh-keystore" ^
    -Djavax.net.ssl.trustStore="%~dp0/config/trustStore.jks" ^
    -Djavax.net.ssl.trustStorePassword="changeit" ^
    org.sensorhub.impl.SensorHub ./config/config.json db

endlocal
