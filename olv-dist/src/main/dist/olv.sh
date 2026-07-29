#!/bin/bash
 ###############################################################################
 # Copyright 2011 Krzysztof Otrebski
 # 
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 # 
 #   http://www.apache.org/licenses/LICENSE-2.0
 # 
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 ###############################################################################

# If you use yery big logfiles you must increase the Xmx value
MEMORY=-Xmx1024m

# Resolve the script location (including symbolic links)
OLV_HOME="$(dirname "$(readlink -f "$0")")"

cd "$OLV_HOME"
[ -n "$TMPDIR" ] || TMPDIR=/tmp

JAVA_VERSION=21
ADD_OPENS="--add-opens java.base/java.util=ALL-UNNAMED"
#SFTP_KEY=-Dvfs.Identities=


# check in subdirectory jre, JAVA_HOME and PATH the java version
# if no correct java version found download java
if [ -x "./jre/bin/java" ] && [ $(./jre/bin/java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.specification.version/ {print $2}') -ge ${JAVA_VERSION} ]
then
    JAVA="./jre/bin/java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] && [ $($JAVA_HOME/bin/java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.specification.version/ {print $2}') -ge ${JAVA_VERSION} ]
then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1 && [ $(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.specification.version/ {print $2}') -ge ${JAVA_VERSION} ]
then
    JAVA="java"
elif [ "$(uname -s)" = "Linux" ]
then
  # Automatic installation of jre

  TARGET_DIR="jre"
  ARCHIVE="Openjdk-${JAVA_VERSION}.tar.gz"

  echo "Download JDK from adoptium.net"
  curl -L -o "${ARCHIVE}" "https://api.adoptium.net/v3/binary/latest/${JAVA_VERSION}/ga/linux/x64/jre/hotspot/normal/eclipse"

  rm -rf "${TARGET_DIR}"
  mkdir -p "${TARGET_DIR}"

  echo "Extracting archive..."
  tar -xzf "${ARCHIVE}" --strip-components=1 -C "${TARGET_DIR}"

  rm -f "${ARCHIVE}"

  echo "Installation completed successfully."
  JAVA="./jre/bin/java"
else
  JAVA="java"
fi

# Check whether Java is installed and available in PATH
command -v ${JAVA} >/dev/null 2>&1 || {
    echo "ERROR: No Java installation found. Please install Java ${JAVA_VERSION}"
    exit 1
}

# Read the Java specification version
JAVA_MAJOR=$(${JAVA} -XshowSettings:properties -version 2>&1 \
    | awk -F'= ' '/java.specification.version/ {print $2}')

# Validate the detected version
[ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -ge ${JAVA_VERSION} ] || {
    echo "ERROR: Java ${JAVA_VERSION} or higher is required. Found: ${JAVA_MAJOR:-unknown}"
    exit 1
}

# Exec is simpler as it eliminates the intervening, useless shell.
# (Makes for easier process troubleshooting).
[  "-batch" = "$1" ] &&
exec $JAVA $MEMORY $ADD_OPENS $SFTP_KEY -jar "$OLV_HOME/olv-exec.jar" $@

# Directory for console.log
mkdir -p logs

# Start OLV detached from the terminal and redirect all console output
# to a log file.
nohup $JAVA $MEMORY $ADD_OPENS $SFTP_KEY \
    -DsingleInstance.startPort= \
    -jar "$OLV_HOME/olv-exec.jar" $@ \
    > logs/console.log 2>&1 < /dev/null &

# GNOME may terminate background processes when the launcher script exits
# immediately. Wait a few seconds to allow the Java application to fully start.
sleep 3
