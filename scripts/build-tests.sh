#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

bash "$ROOT_DIR/scripts/build.sh" || exit 1

JUNIT_API_JAR=$(ls "$ROOT_DIR/lib/junit-jupiter-api-"*.jar 2>/dev/null | head -n 1)

if [ -z "$JUNIT_API_JAR" ]; then
  echo "Не найден junit-jupiter-api-*.jar в $ROOT_DIR/lib"
  exit 1
fi

UNAME_S=$(uname -s 2>/dev/null)
SEP=":"
case "$UNAME_S" in
  CYGWIN*|MINGW*|MSYS*) SEP=";" ;;
esac

CP="$ROOT_DIR/app.jar${SEP}$ROOT_DIR/libs/kotlin-stdlib.jar${SEP}$ROOT_DIR/libs/kotlin-reflect-1.7.10.jar${SEP}$ROOT_DIR/libs/kotlinx-cli-jvm-0.3.6.jar${SEP}$JUNIT_API_JAR"

TEST_FILES=$(find "$ROOT_DIR/tests" -type f -name "*.kt" 2>/dev/null)

if [ -z "$TEST_FILES" ]; then
  echo "Тестовые файлы не найдены в $ROOT_DIR/tests"
  exit 1
fi

kotlinc $TEST_FILES -cp "$CP" -d "$ROOT_DIR/tests.jar"
