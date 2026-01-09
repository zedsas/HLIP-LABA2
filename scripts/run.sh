#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

java -cp "$ROOT_DIR/app.jar;$ROOT_DIR/libs/kotlin-stdlib.jar;$ROOT_DIR/libs/kotlin-reflect-1.7.10.jar;$ROOT_DIR/libs/kotlinx-cli-jvm-0.3.6.jar" AppKt "$@"
