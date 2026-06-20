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

Secrets используются для хранения конфиденциальной информации, такой как пароли, токены и ключи API.

### Пример Secret для accounts-service:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: accounts-service-secrets
  namespace: bank-app
type: Opaque
data:
  SPRING_DATASOURCE_PASSWORD: YmFua19wYXNzd29yZA==  # bank_password
  SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_ACCOUNTS_CLIENT_CLIENT_SECRET: YWNjb3VudHMtc2VjcmV0  # accounts-secret
```

## Переменные окружения

Каждый микросервис получает свою конфигурацию через переменные окружения, которые определены в соответствующих values.yaml файлах Helm чартов.

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