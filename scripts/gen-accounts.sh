#!/bin/bash
set -e

CP="code/app.jar;code/libs/kotlin-stdlib.jar;code/libs/kotlin-reflect-1.7.10.jar;code/libs/kotlinx-cli-jvm-0.3.6.jar"

HASH=$(java -cp "$CP" AppKt --calc-hash)

cat > scripts/fill-accounts.sql <<EOF
INSERT INTO accounts (login, salt, password_hash)
VALUES
 ('player', 'gameSalt', '$HASH');
EOF

echo "Сгенерирован scripts/fill-accounts.sql с hash=$HASH"