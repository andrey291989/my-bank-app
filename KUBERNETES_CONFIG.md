# Конфигурация приложения в Kubernetes

## Обзор

В этой документации описано, как приложение настроено для работы в Kubernetes. Все микросервисы используют ConfigMaps и Secrets для хранения конфигурационных параметров вместо Consul Config.

## ConfigMaps

ConfigMaps используются для хранения неконфиденциальной конфигурационной информации. В нашем приложении они создаются автоматически через Helm чарты.

### Пример ConfigMap для accounts-service:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: accounts-service-config
  namespace: bank-app
data:
  SPRING_PROFILES_ACTIVE: kubernetes
  SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA: accounts_schema
  SERVICES_NOTIFICATIONS_URL: http://notifications-service:8085
```

## Secrets

Secrets используются для хранения конфиденциальной информации, такой как пароли, токены и ключи API. Все пароли хранятся в зашифрованном виде в Kubernetes Secrets и не присутствуют в открытом виде в конфигурационных файлах.

### Пример Secret для accounts-service:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: accounts-service-secrets
  namespace: bank-app
type: Opaque
data:
  database-password: <base64-encoded-password>
```

### Создание Secrets вручную

Для production среды рекомендуется создавать Secrets вручную. Подробнее о создании Secrets смотрите в разделе "Безопасность" ниже.

## Переменные окружения

Каждый микросервис получает свою конфигурацию через переменные окружения, которые определены в соответствующих values.yaml файлах Helm чартов. Конфиденциальные переменные загружаются из Kubernetes Secrets.

### Accounts Service

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://bank-postgresql:5432/bank_db
- `SPRING_DATASOURCE_USERNAME`: bank_user
- `SPRING_DATASOURCE_PASSWORD`: (из Secret)
- `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`: accounts_schema
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`: http://keycloak:8080/realms/bank/protocol/openid-connect/certs
- `SERVICES_NOTIFICATIONS_URL`: http://notifications-service:8085

### Cash Service

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://bank-postgresql:5432/bank_db
- `SPRING_DATASOURCE_USERNAME`: bank_user
- `SPRING_DATASOURCE_PASSWORD`: (из Secret)
- `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`: cash_schema
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`: http://keycloak:8080/realms/bank/protocol/openid-connect/certs
- `SERVICES_ACCOUNTS_URL`: http://accounts-service:8082
- `SERVICES_NOTIFICATIONS_URL`: http://notifications-service:8085

### Transfer Service

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://bank-postgresql:5432/bank_db
- `SPRING_DATASOURCE_USERNAME`: bank_user
- `SPRING_DATASOURCE_PASSWORD`: (из Secret)
- `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`: transfer_schema
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`: http://keycloak:8080/realms/bank/protocol/openid-connect/certs
- `SERVICES_ACCOUNTS_URL`: http://accounts-service:8082
- `SERVICES_NOTIFICATIONS_URL`: http://notifications-service:8085

### Notifications Service

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `SPRING_DATASOURCE_URL`: jdbc:postgresql://bank-postgresql:5432/bank_db
- `SPRING_DATASOURCE_USERNAME`: bank_user
- `SPRING_DATASOURCE_PASSWORD`: (из Secret)
- `SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA`: notifications_schema
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`: http://keycloak:8080/realms/bank/protocol/openid-connect/certs
- `NOTIFICATIONS_DELIVERY_METHOD`: LOG

### Gateway Service

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`: http://keycloak:8080/realms/bank/protocol/openid-connect/certs

### Front UI

Основные переменные окружения:
- `SPRING_PROFILES_ACTIVE`: kubernetes
- `GATEWAY_URL`: http://gateway:8081
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_BANK_CLIENT_ISSUER_URI`: http://keycloak:8080/realms/bank
- `SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_BANK_CLIENT_ISSUER_URI`: http://keycloak:8080/realms/bank

## Service Discovery

В Kubernetes Service Discovery реализован через встроенный механизм DNS. Каждый сервис доступен по своему имени внутри кластера:

- `accounts-service:8082`
- `cash-service:8083`
- `transfer-service:8084`
- `notifications-service:8085`
- `gateway:8081`
- `front-ui:8080`
- `bank-postgresql:5432`
- `keycloak:8080`

## Ingress

Для доступа к приложению извне кластера используется Ingress контроллер. Мы настроили два основных пути:

1. `/` - направляет на front-ui сервис
2. `/api` - направляет на gateway сервис

## Масштабирование

Каждый микросервис может быть масштабирован независимо, изменяя параметр `replicaCount` в соответствующем values.yaml файле Helm чарта.

## Мониторинг и логирование

Все микросервисы имеют встроенные эндпоинты для мониторинга:
- `/actuator/health` - проверка состояния сервиса
- `/actuator/info` - информация о сервисе

Логи доступны через команду `kubectl logs`.

## Безопасность

Все конфиденциальные данные хранятся в Kubernetes Secrets и не присутствуют в открытом виде в конфигурационных файлах. При развертывании через CI/CD пайплайн (Jenkins) пароли передаются безопасно через механизмы учетных данных Jenkins.

### Создание Secrets

Для создания Secrets вручную используйте скрипт `create-secrets.sh` или команды kubectl:

```bash
# Создание Secret для базы данных
kubectl create secret generic bank-db-secret \
  --from-literal=user-password='ВАШ_ПАРОЛЬ' \
  --from-literal=postgres-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app

# Создание Secret для Keycloak
kubectl create secret generic keycloak-secret \
  --from-literal=admin-password='ВАШ_ПАРОЛЬ' \
  --from-literal=database-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app

# Создание Secret для микросервисов
kubectl create secret generic accounts-service-secret \
  --from-literal=database-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app

kubectl create secret generic cash-service-secret \
  --from-literal=database-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app

kubectl create secret generic transfer-service-secret \
  --from-literal=database-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app

kubectl create secret generic notifications-service-secret \
  --from-literal=database-password='ВАШ_ПАРОЛЬ' \
  --namespace bank-app
```

**Важно:** В production среде всегда используйте сильные уникальные пароли и регулярно их меняйте.