#!/bin/sh
#
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
#

set -e

PROG="$0"
while [ -h "$PROG" ]; do
  ls=$(ls -ld "$PROG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' >/dev/null; then
    PROG="$link"
  else
    PROG=$(dirname "$PROG")"/$link"
  fi
done

SAVED="$(cd -P "$(dirname "$PROG")" && pwd)"
cd "$SAVED"

DEFAULT_JVM_OPTS=""
JAVA_OPTS="${JAVA_OPTS:-}"
GRADLE_OPTS="${GRADLE_OPTS:-}"

exec "${JAVA_HOME}/bin/java" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$SAVED/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
