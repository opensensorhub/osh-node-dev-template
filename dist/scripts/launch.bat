java -Xmx2g ^
    -Dlogback.configurationFile=./config/logback.xml ^
    -cp "lib/*" ^
    -Djava.system.class.loader="org.sensorhub.utils.NativeClassLoader" ^
    -Djavax.net.ssl.keyStore="./config/osh-keystore.p12" ^
    -Djavax.net.ssl.keyStorePassword="osh-keystore" ^
    -Djavax.net.ssl.trustStore="%~dp0/config/trustStore.jks" ^
    -Djavax.net.ssl.trustStorePassword="changeit" ^
    org.sensorhub.impl.SensorHub ./config/config.json db
