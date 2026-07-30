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
| **Apache Kafka** | 9092 | Распределённая платформа потоковой обработки сообщений |
| **ZooKeeper** | 2181 | Координационный сервис для Kafka |
| **Keycloak** | 9000 | OAuth2 сервер авторизации (Admin UI) |
| **PostgreSQL** | 5432 | База данных |

### Компоненты наблюдаемости (Observability)

| Компонент | Порт | Описание |
|-----------|------|----------|
| **Zipkin** | 9411 | Распределённая трассировка (Micrometer Tracing + Brave) |
| **Prometheus** | 9090 | Сбор метрик (Actuator + Micrometer) |
| **Alertmanager** | 9093 | Маршрутизация алертов Prometheus |
| **Grafana** | 3000 | Дашборды метрик (admin/admin) |
| **Elasticsearch** | 9200 | Хранение логов |
| **Logstash** | 5000 | Приём и обработка логов (TCP, JSON) |
| **Kibana** | 5601 | Визуализация логов |

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
├── zipkin/                     # Конфигурация Zipkin (трассировка)
│   └── README.md
├── prometheus/                 # prometheus.yml, alert.rules.yml, alertmanager.yml
├── grafana/                    # provisioning (datasource, dashboards) + JSON-дашборды
│   ├── provisioning/
│   └── dashboards/
├── elk/                        # Elasticsearch + Logstash + Kibana
│   └── logstash/
├── helm-charts/
│   ├── Jenkinsfile              # Jenkinsfile для развертывания Apache Kafka
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
├── Jenkinsfile                  # Основной Jenkinsfile для всего приложения
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

### Запуск с Apache Kafka
Приложение теперь использует Apache Kafka для асинхронной обработки уведомлений.
Kafka и ZooKeeper будут автоматически запущены вместе с другими сервисами.

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

## Локальная сборка и тесты (без Docker)

Требуется **JDK 21** и Maven. Сборка всех модулей и запуск тестов:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
./mvnw clean test      # или: mvn clean test
```

Отдельный сервис можно запустить локально (при поднятой инфраструктуре в Docker):

```bash
mvn -pl accounts-service spring-boot:run
```

При локальном запуске без Docker трейсы и логи по умолчанию идут на
`localhost:9411` (Zipkin) и `localhost:5000` (Logstash); адреса переопределяются
переменными `ZIPKIN_ENDPOINT`, `LOGSTASH_HOST`, `LOGSTASH_PORT`.

### Тесты

* **Unit-тесты** (Mockito) — бизнес-логика сервисов.
* **Slice-тесты контроллеров** (`@WebMvcTest` + `spring-security-test`) — валидация
  запросов (400) и защита эндпоинтов (401 без JWT). В тестовом `bootstrap.yml`
  отключён Consul.
* **EmbeddedKafka** — round-trip `NotificationEvent` через in-JVM брокер (без Docker).
* **Testcontainers** (`AccountsPersistenceIT`) — старт контекста + применение
  Flyway-миграций + Hibernate `validate` на реальном PostgreSQL. Требует Docker;
  без него тест автоматически пропускается (`disabledWithoutDocker = true`).

## Наблюдаемость: трассировка, метрики, логирование

Все компоненты наблюдаемости поднимаются автоматически вместе с приложением
(`docker-compose up -d`). Их конфигурация хранится в подпроектах `zipkin/`,
`prometheus/`, `grafana/`, `elk/`.

### Точки доступа

| Инструмент | URL | Назначение |
|------------|-----|-----------|
| Zipkin | http://localhost:9411 | Трейсы запросов между сервисами, в БД и Kafka |
| Prometheus | http://localhost:9090 | Метрики и правила алертов (*Status → Targets*, *Alerts*) |
| Alertmanager | http://localhost:9093 | Активные алерты |
| Grafana | http://localhost:3000 | Дашборды (логин `admin` / `admin`), папка **Bank** |
| Kibana | http://localhost:5601 | Логи микросервисов и Front UI |

### Трассировка (Zipkin)

* Поставка трейсов — через **Micrometer Tracing** (`micrometer-tracing-bridge-brave`,
  `zipkin-reporter-brave`).
* Трассируются входящие/исходящие HTTP-запросы, запросы в БД
  (`datasource-micrometer`) и в Apache Kafka (observation у `KafkaTemplate` и
  listener-контейнера).
* Front UI генерирует `trace id`; далее `trace id` и `span id` пробрасываются в
  заголовках между сервисами. Те же `traceId`/`spanId` попадают в логи (Kibana),
  что позволяет связать логи с трейсами. Подробнее — `zipkin/README.md`.

### Метрики (Prometheus + Grafana)

* Метрики отдаёт каждый сервис по `/actuator/prometheus`.
* Стандартные: HTTP (RPS, 4xx, 5xx, персентили таймингов), JVM (память, CPU, GC,
  потоки), метрики Spring Boot.
* Кастомные бизнес-метрики:
  * `bank_cash_withdrawal_failed_total{login}` — неуспешные снятия;
  * `bank_transfer_failed_total{from_login,to_login}` — неуспешные переводы;
  * `bank_notification_send_failed_total{login}` — невозможность отправки уведомления.
* Дашборды Grafana: HTTP-метрики, JVM-метрики, бизнес-метрики (папка **Bank**).
* Алерты по превышению порогов настроены в Prometheus/Alertmanager
  (`prometheus/alert.rules.yml`). Подробнее — `prometheus/README.md`, `grafana/README.md`.

### Логирование (ELK)

* Логирование через **SLF4J + Logback** в едином JSON-формате
  (`logstash-logback-encoder`, паттерн Microservice Chassis — `logback-spring.xml`
  в каждом модуле).
* Логи отправляются в **Logstash** (TCP `5000`), фильтруются (маскирование паролей и
  номеров счетов/карт) и пишутся в **Elasticsearch**, визуализируются в **Kibana**.
* В каждой записи присутствуют `traceId` и `spanId` для связи с трейсами в Zipkin.
* Уровни: `ERROR` (ошибки), `WARN` (неуспешные бизнес-операции), `INFO` (ключевые
  операции), `DEBUG` (диагностика). Подробнее — `elk/README.md`.

### Быстрая проверка

```bash
# метрики сервиса
curl -s http://localhost:8082/actuator/prometheus | head

# цели Prometheus (должны быть UP)
open http://localhost:9090/targets

# трейсы — выполните операцию во фронте (http://localhost:8080), затем:
open http://localhost:9411

# логи — в Kibana создайте data view "bank-logs-*" (timestamp @timestamp) и откройте Discover
open http://localhost:5601
```

> Первый старт Elasticsearch/Kibana может занять 1–2 минуты. Kibana требует
> однократного создания data view `bank-logs-*` (см. `elk/README.md`).

## Развертывание в Kubernetes (Helm)

### Предварительные требования
- Установленный Minikube или другой локальный Kubernetes кластер
- Установленный kubectl
- Установленный Helm

### Архитектура
Приложение использует Apache Kafka для асинхронной обработки уведомлений между микросервисами.
Kafka разворачивается как часть Helm chart с использованием Bitnami Helm chart в качестве зависимости.

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

Также добавлен отдельный Jenkinsfile в директории `helm-charts/` для развертывания Apache Kafka в Kubernetes кластер.

### Этапы пайплайна:
1. **Checkout** - Получение исходного кода из репозитория
2. **Build** - Сборка микросервисов с помощью Maven
3. **Test** - Запуск модульных и интеграционных тестов
4. **Build Docker Images** - Создание Docker образов для всех микросервисов
5. **Push Docker Images** - Загрузка образов в Docker Registry
6. **Deploy Kafka** - Развертывание Apache Kafka в отдельном namespace
7. **Helm Lint** - Проверка Helm чартов
8. **Deploy to Kubernetes** - Развертывание приложения в Kubernetes кластер
9. **Helm Test** - Запуск тестов Helm чартов
10. **Verify Deployment** - Проверка успешности развертывания

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

Также можно создать отдельный Pipeline job для развертывания только Apache Kafka, указав путь `helm-charts/Jenkinsfile`.

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