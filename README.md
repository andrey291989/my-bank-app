# My Bank App - Микросервисное банковское приложение

### Микросервисы

| Сервис | Порт | Описание |
|--------|------|----------|
| **Front UI** | 8080 | Веб-интерфейс пользователя (Thymeleaf) |
| **Gateway API** | 8081 | Spring Cloud Gateway, маршрутизация, Circuit Breaker |
| **Accounts Service** | 8082 | Управление аккаунтами и счетами |
| **Cash Service** | 8083 | Пополнение и снятие денег |
| **Transfer Service** | 8084 | Переводы между счетами |
| **Notifications Service** | 8085 | Отправка уведомлений (лог/email) |
| **Consul** | 8500 | Service Discovery + Config (UI) |
| **Keycloak** | 9000 | OAuth2 сервер авторизации (Admin UI) |
| **PostgreSQL** | 5432 | База данных |

## Быстрый старт (Docker)

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/my-bank-app.git
cd my-bank-app
```
## Структура проекта


```my-bank-app/
├── docker-compose.yml
├── init-scripts/
│   └── 01-init-schemas.sql
├── keycloak-config/
│   └── bank-realm.json
├── front-ui/
│   ├── Dockerfile
│   └── pom.xml
├── gateway/
│   ├── Dockerfile
│   └── pom.xml
├── accounts-service/
│   ├── Dockerfile
│   └── pom.xml
├── cash-service/
│   ├── Dockerfile
│   └── pom.xml
├── transfer-service/
│   ├── Dockerfile
│   └── pom.xml
├── notifications-service/
│   ├── Dockerfile
│   └── pom.xml
└── README.md
```

## Сборка и запуск

### Сборка всех образов (первый запуск может занять 5-10 минут)
docker-compose build

### Запуск всех сервисов
docker-compose up -d

### Просмотр логов (убедитесь, что все сервисы запустились без ошибок)
docker-compose logs -f

### Просмотр логов конкретного сервиса
docker-compose logs -f accounts-service

### Статус всех контейнеров
docker-compose ps
