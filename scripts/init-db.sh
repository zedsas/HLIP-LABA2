#!/usr/bin/env bash
set -e

DB_PATH="./scripts/appdb"

./scripts/gen-accounts.sh

./mvnw -q -DincludeArtifactIds=h2 -DoutputDirectory=target/deps dependency:copy-dependencies

H2_JAR=$(ls target/deps/h2-*.jar | head -n 1)

java -cp "$H2_JAR" org.h2.tools.RunScript \
  -url "jdbc:h2:file:$DB_PATH;AUTO_SERVER=TRUE" \
  -user "sa" \
  -password "" \
  -script "./scripts/init.sql"

java -cp "$H2_JAR" org.h2.tools.RunScript \
  -url "jdbc:h2:file:$DB_PATH;AUTO_SERVER=TRUE" \
  -user "sa" \
  -password "" \
  -script "./scripts/fill-accounts.sql"

java -cp "$H2_JAR" org.h2.tools.RunScript \
  -url "jdbc:h2:file:$DB_PATH;AUTO_SERVER=TRUE" \
  -user "sa" \
  -password "" \
  -script "./scripts/fill.sql"
