#!/bin/sh

# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Resolve APP_HOME to the directory containing this script.
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SAVED="`pwd`"
cd `dirname "$PRG"` >/dev/null
APP_HOME=`pwd -P`
cd "$SAVED" >/dev/null

DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.14.3/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_SHA256="7d3a4ac4de1c32b59bc6a4eb8ecb8e612ccd0cf1ae1e99f66902da64df296172"

ensure_wrapper_jar() {
    if [ -f "$WRAPPER_JAR" ]; then
        return
    fi

    echo "Downloading Gradle wrapper runtime..." >&2
    mkdir -p "`dirname "$WRAPPER_JAR"`"

    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR" || rm -f "$WRAPPER_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR" || rm -f "$WRAPPER_JAR"
    else
        echo "Neither curl nor wget is available to download the Gradle wrapper jar." >&2
        exit 1
    fi

    if [ ! -f "$WRAPPER_JAR" ]; then
        echo "Failed to download the Gradle wrapper jar from $WRAPPER_URL" >&2
        exit 1
    fi
}

verify_wrapper_checksum() {
    if ! command -v sha256sum >/dev/null 2>&1; then
        return
    fi

    actual="`sha256sum "$WRAPPER_JAR" | awk '{print $1}'`"
    if [ "$actual" != "$WRAPPER_SHA256" ]; then
        echo "Gradle wrapper jar checksum mismatch. Retrying download..." >&2
        rm -f "$WRAPPER_JAR"
        ensure_wrapper_jar
        actual="`sha256sum "$WRAPPER_JAR" | awk '{print $1}'`"
        if [ "$actual" != "$WRAPPER_SHA256" ]; then
            echo "Gradle wrapper jar failed checksum verification (got $actual)." >&2
            exit 1
        fi
    fi
}

ensure_wrapper_jar
verify_wrapper_checksum
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  MINGW* ) msys=true ;;
  MSYS* ) msys=true ;;
  Darwin* ) darwin=true ;;
  NONSTOP* ) nonstop=true ;;
  * ) ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
fi

if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

if $cygwin ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -Dorg.gradle.appname=$APP_BASE_NAME -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
