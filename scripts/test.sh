#!/usr/bin/env bash
set -e

./scripts/build.sh

JAR="target/laba7-1.0.0.jar"

declare -a cases=(
  "--login player --password qwerty --action read --resource data --volume 1|0"
  "--help|1"
  "--login player --password wrongpass --action read --resource data --volume 1|2"
  "--login unknown_user --password qwerty --action read --resource data --volume 1|3"
  "--login player --password qwerty --action delete --resource data --volume 1|4"
  "--login player --password qwerty --action execute --resource data --volume 1|5"
  "--login player --password qwerty --action read --resource nonexistent --volume 1|6"
  "--login player --password qwerty --action read --resource invalid..path --volume 1|7"
  "--login player --password qwerty --action read --resource data --volume 100|8"
  "--login player --password qwerty --action write --resource data.logs --volume 1|0"
)

success_count=0
total=${#cases[@]}

echo "Запуск $total тестов..."

for i in "${!cases[@]}"; do
  IFS='|' read -r args expected <<< "${cases[$i]}"

  if [ -z "$args" ]; then
    java -jar "$JAR" > /dev/null 2>&1
  else
    java -jar "$JAR" $args > /dev/null 2>&1
  fi

  code=$?
  exp=${expected:-0}

  if [ $code -eq $exp ]; then
    echo "Тест $((i+1)): OK (код $code)"
    ((success_count++))
  else
    echo "Тест $((i+1)): FAIL (получен $code, ожидался $exp) — аргументы: $args"
  fi
done

echo
echo "Результат: $success_count из $total тестов пройдены"
