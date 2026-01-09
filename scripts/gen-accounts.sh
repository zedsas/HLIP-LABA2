#!/usr/bin/env bash
set -e

./scripts/build.sh

JAR="target/laba7-1.0.0.jar"
HASH=$(java -jar "$JAR" --calc-hash)

cat > scripts/fill-accounts.sql <<EOF
INSERT INTO accounts (login, salt, password_hash)
VALUES
 ('player', 'gameSalt', '$HASH');
EOF
