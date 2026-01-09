#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

JUNIT_CONSOLE_JAR=$(ls "$ROOT_DIR/lib/junit-platform-console-standalone-"*.jar 2>/dev/null | head -n 1)

if [ -z "$JUNIT_CONSOLE_JAR" ]; then
  echo "Не найден junit-platform-console-standalone-*.jar в $ROOT_DIR/lib"
  exit 1
fi

bash "$ROOT_DIR/scripts/build-tests.sh" || exit 1

UNAME_S=$(uname -s 2>/dev/null)
SEP=":"
case "$UNAME_S" in
  CYGWIN*|MINGW*|MSYS*) SEP=";" ;;
esac

CP="$ROOT_DIR/tests.jar${SEP}$ROOT_DIR/app.jar${SEP}$ROOT_DIR/libs/kotlin-stdlib.jar${SEP}$ROOT_DIR/libs/kotlin-reflect-1.7.10.jar${SEP}$ROOT_DIR/libs/kotlinx-cli-jvm-0.3.6.jar"

java -jar "$JUNIT_CONSOLE_JAR" --class-path "$CP" --scan-classpath --include-classname ".*"
