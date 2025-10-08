# Шаги по интеграции SonarCloud

1. **Регистрация**  
   Заходим на [https://sonarcloud.io](https://sonarcloud.io) через аккаунт GitHub.

2. **Подключаем репозиторий**  
   Нажимаем «Analyze new project», выбираем свой репозиторий и копируем:
   - `sonar.projectKey`
   - `sonar.organization`

3. **Файл настроек**  
   В корне проекта создаём файл `sonar-project.properties`:
   ```properties
   sonar.projectKey=ваш_ключ_проекта
   sonar.organization=ваша_организация
   sonar.sources=.
   sonar.language=kotlin
   sonar.sourceEncoding=UTF-8
   ```

4. **GitHub Secrets**  
   В настройках репозитория (Settings → Secrets and variables → Actions) добавляем:
   - **Name**: `SONAR_TOKEN`  
   - **Value**: токен из SonarCloud (в личном кабинете → Security)

5. **Workflow для анализа**  
   Создаём файл `.github/workflows/sonarcloud.yml`:
   ```yaml
   name: SonarCloud Analysis

   on:
     push:
       branches: [ main ]
     pull_request:
       types: [ opened, synchronize ]

   jobs:
     sonarcloud:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
           with:
             fetch-depth: 0

         - name: Set up JDK 17
           uses: actions/setup-java@v3
           with:
             java-version: '17'
             distribution: 'temurin'

         - name: Build project
           run: ./build.sh

         - name: Run SonarCloud Scan
           uses: SonarSource/sonarcloud-scan@master
           env:
             GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
             SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

6. **Готово**  
   После настройки каждый push в `main` и каждый pull request будут автоматически анализироваться SonarCloud.
