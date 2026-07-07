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
| **Keycloak** | 9000 | OAuth2 сервер авторизации (Admin UI) |
| **PostgreSQL** | 5432 | База данных |

## Быстрый старт (Docker)

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/my-bank-app.git
cd my-bank-app
```

### Структура проекта

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
├── helm-charts/
│   └── bank-app-chart/
│       ├── Chart.yaml
│       ├── values.yaml
│       ├── requirements.yaml
│       ├── charts/
│       │   ├── front-ui/
│       │   ├── gateway/
│       │   ├── accounts-service/
│       │   ├── cash-service/
│       │   ├── transfer-service/
│       │   ├── notifications-service/
│       │   ├── postgresql/
│       │   └── keycloak/
│       └── templates/
├── Jenkinsfile
├── build-images.sh
├── deploy.sh
├── test-helm.sh
├── update-config.sh
├── create-secrets.sh
├── KUBERNETES_CONFIG.md
└── README.md
```

## Сборка и запуск (Docker)

### Сборка всех образов (первый запуск может занять 5-10 минут)
```bash
docker-compose build
```

### Запуск всех сервисов
```bash
docker-compose up -d
```

### Просмотр логов (убедитесь, что все сервисы запустились без ошибок)
```bash
docker-compose logs -f
```

### Просмотр логов конкретного сервиса
```bash
docker-compose logs -f accounts-service
```

### Статус всех контейнеров
```bash
docker-compose ps
```

## Развертывание в Kubernetes (Helm)

### Предварительные требования
- Установленный Minikube или другой локальный Kubernetes кластер
- Установленный kubectl
- Установленный Helm

### Безопасность
Все конфиденциальные данные (пароли, токены) хранятся в Kubernetes Secrets и не присутствуют в открытом виде в конфигурационных файлах. Подробнее см. в [KUBERNETES_CONFIG.md](KUBERNETES_CONFIG.md).

### Создание Secrets (обязательно для production)
Перед развертыванием в production среде создайте Secrets с вашими реальными паролями:

```bash
# Используйте скрипт для создания тестовых Secrets (только для разработки!)
./create-secrets.sh

# ИЛИ создайте Secrets вручную с вашими реальными паролями
kubectl create secret generic bank-db-secret \
  --from-literal=user-password='ВАШ_ПАРОЛЬ' \
  --from-literal=postgres-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app
```

Подробные инструкции по созданию Secrets смотрите в [KUBERNETES_CONFIG.md](KUBERNETES_CONFIG.md).

### Сборка Docker образов
```bash
./build-images.sh
```

### Загрузка образов в Minikube (если используется Minikube)
```bash
minikube image load bank/front-ui:latest
minikube image load bank/gateway:latest
minikube image load bank/accounts-service:latest
minikube image load bank/cash-service:latest
minikube image load bank/transfer-service:latest
minikube image load bank/notifications-service:latest
```

### Развертывание приложения с помощью Helm
```bash
./deploy.sh
```

### Доступ к приложению
После развертывания добавьте следующие записи в ваш файл /etc/hosts:
```
127.0.0.1 bank.local
127.0.0.1 api.bank.local
```

Затем откройте в браузере:
- Фронтенд: http://bank.local
- API Gateway: http://api.bank.local/api

### Просмотр статуса развертывания
```bash
kubectl get pods -n bank-app
kubectl get services -n bank-app
kubectl get ingress -n bank-app
```

### Просмотр логов сервисов
```bash
kubectl logs -n bank-app -l app.kubernetes.io/name=front-ui
kubectl logs -n bank-app -l app.kubernetes.io/name=gateway
kubectl logs -n bank-app -l app.kubernetes.io/name=accounts-service
```

### Удаление приложения
```bash
helm uninstall bank-app -n bank-app
```

## CI/CD Pipeline (Jenkins)

Проект включает Jenkinsfile для автоматической сборки, тестирования и развертывания приложения.

### Этапы пайплайна:
1. **Checkout** - Получение исходного кода из репозитория
2. **Build** - Сборка микросервисов с помощью Maven
3. **Test** - Запуск модульных и интеграционных тестов
4. **Build Docker Images** - Создание Docker образов для всех микросервисов
5. **Push Docker Images** - Загрузка образов в Docker Registry
6. **Helm Lint** - Проверка Helm чартов
7. **Deploy to Kubernetes** - Развертывание приложения в Kubernetes кластер
8. **Helm Test** - Запуск тестов Helm чартов
9. **Verify Deployment** - Проверка успешности развертывания

### Настройка Jenkins:
1. Установите Jenkins и необходимые плагины:
   - Kubernetes CLI plugin
   - Docker Pipeline plugin
   - Pipeline plugin
   - Git plugin
   - Email Extension plugin

2. Настройте учетные данные в Jenkins:
   - `docker-hub-credentials` - учетные данные для Docker Hub
   - `kubeconfig-credentials` - файл kubeconfig для доступа к Kubernetes кластеру

3. Создайте новый Pipeline job и укажите путь к Jenkinsfile в репозитории

### Переменные окружения:
- `DOCKER_REGISTRY` - адрес Docker registry (по умолчанию docker.io)
- `DOCKER_REPO` - имя репозитория (по умолчанию bank)
- `HELM_RELEASE_NAME` - имя релиза Helm (по умолчанию bank-app)
- `HELM_NAMESPACE` - namespace в Kubernetes (по умолчанию bank-app)

### Запуск пайплайна вручную:
```bash
# Локальное тестирование Jenkinsfile
jenkinsfile-runner -w /path/to/jenkins/war -p /path/to/plugins.txt -f Jenkinsfile
```