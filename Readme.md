# Лабораторная работа №2 — Система контроля доступа к ресурсам

## Авторы
- Тусюк Александр
- Зорин Олег

## Описание
Консольное приложение проверяет права пользователя на ресурс (иерархия + ACL) и возвращает код выполнения.

## Окружение
- Java 11+
- Kotlin compiler

## Библиотеки
### libs/ (приложение)
- kotlin-reflect-1.7.10.jar
- kotlin-stdlib.jar
- kotlinx-cli-jvm-0.3.6.jar
- junit-platform-console-standalone-*.jar (запуск тестов в терминале) [web:132]
- junit-jupiter-api-*.jar, junit-jupiter-engine-*.jar (JUnit Jupiter) [web:75]

## Сборка и запуск

### Сборка приложения
- bash scripts/build.sh

### Запуск приложения
- bash scripts/run.sh --login player --password qwerty --action read --resource data --volume 5

### Справка
- bash scripts/run.sh --help

### Интеграционные тесты (exit-коды)
- bash scripts/test.sh

### Модульные тесты (JUnit)
- bash scripts/run-tests.sh

Запуск выполняется через JUnit ConsoleLauncher со сканированием classpath (--scan-classpath).

## Результаты выполнения тестов
### Что пришлось изменить в проекте
- Добавлена папка tests/ и помечена как Test Sources Root в IntelliJ IDEA. [web:88]
- Добавлены JUnit JAR в lib/ и скрипты build-tests.sh/run-tests.sh для сборки и запуска тестов. [web:132]### Какие классы пока не тестируются и почему
- App.kt (CLI: ввод/вывод, завершение процесса) — удобнее проверять интеграционно через запуск приложения и анализ exit-кода.