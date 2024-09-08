java -Xmx2g -Dlogback.configurationFile=./config/logback.xml -cp "lib/*" -Djava.system.class.loader="org.sensorhub.utils.NativeClassLoader" org.sensorhub.impl.SensorHub ./config/config.json db
