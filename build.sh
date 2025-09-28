#!/bin/bash
kotlinc App.kt -cp "libs/kotlin-stdlib.jar;libs/kotlin-reflect-1.7.10.jar;libs/kotlinx-cli-jvm-0.3.6.jar" -d app.jar
if [ $? -eq 0 ]; then
    echo "✅ Сборка успешна: app.jar создан"
else
    echo "❌ Ошибка при сборке"
    exit 1
fi