# Лабораторная работа №8

## Авторы
- Тусюк Александр
- Зорин Олег

## Что это
Консольное приложение для проверки доступа пользователя к ресурсам.  
Хранение данных: H2.  
Доступ к данным: Spring Data JPA (ORM).  
Схема БД: Flyway (SQL-миграции).

## Структура
- `scripts/` — сборка/запуск/тесты
- `src/main/kotlin/` — исходный код
- `src/main/resources/db.migration/` — миграции Flyway

## Запуск

### 1) Перейти в папку проекта

- cd /c/Путь/До/Папки/laba7
### 2) Собрать проект

- ./scripts/build.sh

### 3) Сгенерировать хеш для пользователя player (автоматически)
Команда сама посчитает хеш и запишет его в application.properties (в placeholder Flyway):

- ./scripts/gen-accounts.sh
### 4) Если база уже создавалась ранее — удалить старую file-БД

- ./scripts/appdb*

### 5) Запуск приложения
Пример:
./scripts/run.sh --login player --password qwerty --action read --resource data --volume 1

### 6) Запуск тестов

   - ./scripts/test.sh
