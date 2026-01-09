#!/usr/bin/env bash
set -e

./scripts/build.sh

JAR="target/laba7-1.0.0.jar"
HASH=$(java -jar "$JAR" --calc-hash)

PROP_FILE="src/main/resources/application.properties"

if grep -q "^spring\.flyway\.placeholders\.playerHash=" "$PROP_FILE"; then
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s/^spring\.flyway\.placeholders\.playerHash=.*/spring.flyway.placeholders.playerHash=$HASH/" "$PROP_FILE"
  else
    sed -i "s/^spring\.flyway\.placeholders\.playerHash=.*/spring.flyway.placeholders.playerHash=$HASH/" "$PROP_FILE"
  fi
else
  echo "" >> "$PROP_FILE"
  echo "spring.flyway.placeholders.playerHash=$HASH" >> "$PROP_FILE"
fi

echo "playerHash updated in $PROP_FILE"
