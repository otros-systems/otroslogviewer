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

CURRENT_DIR=`pwd`
# based on http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in

# This is Bash-specific.  We should support all UNIXes.
#SOURCE="${BASH_SOURCE[0]}"
#while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
#OLV_HOME="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
#cd $OLV_HOME

OLV_HOME="$0"
while [ -h "$OLV_HOME" ] ; do OLV_HOME="$(readlink "$OLV_HOME")"; done
case "$OLV_HOME" in
/*) OLV_HOME="${OLV_HOME%/*}";; */*) OLV_HOME="$PWD/${OLV_HOME%/*}";; *) OLV_HOME="$PWD";;
esac
case "$OLV_HOME" in *?/.) OLV_HOME="${OLV_HOME%/.}"; esac

cd "$OLV_HOME"
[ -n "$TMPDIR" ] || TMPDIR=/tmp

MEMORY=-Xmx1024m
LOG_PROPERTIES=-Djava.util.logging.config.file=logging.properties
#SFTP_KEY=-Dvfs.Identities=

if [ -n "$JAVA_HOME" ]
then
 JAVA="$JAVA_HOME/bin/java"
else
 JAVA=java
fi

# Exec is simpler as it eliminates the intervening, useless shell.
# (Makes for easier process troubleshooting).
[  "-batch" = "$1" ] &&
exec $JAVA $LOG_PROPERTIES $MEMORY $SFTP_KEY -DOLV_HOME="$OLV_HOME" -DCURRENT_DIR="$CURRENT_DIR" -jar "$OLV_HOME/lib/OtrosStarter.jar" $@
# we need to be sure we are writing to a directory where we have write access, /tmp is one of them but app directory is clearly not
# If we want more than one user on a shared system to be able to run OLV, then make log file user-specific (at least)
exec $JAVA $LOG_PROPERTIES $MEMORY $SFTP_KEY -DOLV_HOME="$OLV_HOME" -DCURRENT_DIR="$CURRENT_DIR" -jar "$OLV_HOME/lib/OtrosStarter.jar" $@ > "${TMPDIR}/olv-${LOGNAME}.log" 2>&1 &
