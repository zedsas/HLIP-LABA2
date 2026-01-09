#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

CP="$ROOT_DIR/libs/kotlin-stdlib.jar;$ROOT_DIR/libs/kotlin-reflect-1.7.10.jar;$ROOT_DIR/libs/kotlinx-cli-jvm-0.3.6.jar"

KOTLIN_FILES=$(find "$ROOT_DIR" -type f -name "*.kt" \
  ! -path "*/.idea/*" \
  ! -path "*/libs/*" \
  ! -path "*/lib/*" \
  ! -path "*/out/*" \
  ! -path "*/tests/*")

kotlinc $KOTLIN_FILES -cp "$CP" -d "$ROOT_DIR/app.jar"

if [ $? -eq 0 ]; then
    echo "Сборка успешна: app.jar создан"
else
    echo "Ошибка при сборке"
    exit 1
fi
