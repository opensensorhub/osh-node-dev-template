#!/bin/bash

################################################################################
##
##  OpenSensorHub container entry point.
##
##  Before starting the Java process, this script does the following:
##
##    1. It emits a banner and attempts to show the hostname and IP address of
##       the container (for testing/debugging purposes).
##
##    2. It copies default configuration files (config.json and logback.xml)
##       into place if they don't already exist in the config volume.
##
################################################################################

# Emit a banner that is easy to spot in logs
echo "
   ____                    _____                           _    _       _
  / __ \\                  / ____|                         | |  | |     | |
 | |  | |_ __   ___ _ __ | (___   ___ _ __  ___  ___  _ __| |__| |_   _| |__
 | |  | | '_ \\ / _ \\ '_ \\ \\___ \\ / _ \\ '_ \\/ __|/ _ \\| '__|  __  | | | | '_ \\
 | |__| | |_) |  __/ | | |____) |  __/ | | \\__ \\ (_) | |  | |  | | |_| | |_) |
  \\____/| .__/ \\___|_| |_|_____/ \\___|_| |_|___/\\___/|_|  |_|  |_|\\__,_|_.__/
        | |                                                        ___   ___
        |_|                                                  _  __|_  | / _ \\
                                                            | |/ / __/_/ // /
                                                            |___/____(_)___/
"


##
##  Get the path to the directory where this script lives. This is used to
##  invoke other startup scripts without assuming our current working directory.
##
SCRIPT_DIR="$(cd "$(dirname "$0")"; pwd)"

# Make sure all the necessary certificates are trusted by the system.
"$SCRIPT_DIR/load_trusted_certs.sh"

if [ -z "$OSH_HOME" ]; then
  echo "OSH_HOME environment variable is not set."
  exit 1
fi
CONFIG_DIR="$OSH_HOME/config"

##
##  Attempt to print out some information about the hostname and IP address of
##  the container.
##

if command -v getent &> /dev/null ; then
  # If our runtime has getent, use it to print our IP
  IP=$( getent ahosts $HOSTNAME | head -1 | cut -f 1 -d ' ' )
  echo ""
  echo "Hostname:          $HOSTNAME"
  echo "IP Address:        $IP"
elif command -v grep &> /dev/null ; then
  # Otherwise look for our hostname in /etc/hosts as a fallback.
  echo ""
  echo "IP address and hostname:"
  grep $HOSTNAME /etc/hosts
fi

echo ""

# From here on, quit if something fails.
set -e

##
##  Look for "config.json" and "logback.xml" in the config directory. If those
##  files don't exist, create them by copying some defaults.
##

DEFAULT_CONFIG_DIR="$OSH_HOME/defaultconfig"
CONFIG_JSON=config.json
LOGBACK_XML=logback.xml

if [ ! -f "$CONFIG_DIR/$CONFIG_JSON" ]; then
  echo "Config file \"$CONFIG_JSON\" does not exist. Creating from default."
  cp -v "$DEFAULT_CONFIG_DIR/$CONFIG_JSON" "$CONFIG_DIR/$CONFIG_JSON"
else
  echo "Config file \"$CONFIG_JSON\" already exists. Leaving it alone."
fi

if [ ! -f "$CONFIG_DIR/$LOGBACK_XML" ]; then
  echo "Config file \"$LOGBACK_XML\" does not exist. Creating from default."
  cp -v "$DEFAULT_CONFIG_DIR/$LOGBACK_XML" "$CONFIG_DIR/$LOGBACK_XML"
else
  echo "Config file \"$LOGBACK_XML\" already exists. Leaving it alone."
fi

# Start the node
java -Xmx2g \
	-Dlogback.configurationFile=./config/logback.xml \
	-cp "lib/*:userclasses:userlib/*" \
	-Djava.system.class.loader="org.sensorhub.utils.NativeClassLoader" \
	-Djavax.net.ssl.keyStore="./config/osh-keystore.p12" \
	-Djavax.net.ssl.keyStorePassword="atakatak" \
	-Djavax.net.ssl.trustStore="$SCRIPT_DIR/trustStore.jks" \
	-Djavax.net.ssl.trustStorePassword="changeit" \
	org.sensorhub.impl.SensorHub ./config/config.json ./db
