# Prometheus — сбор метрик и алерты

Подпроект содержит конфигурацию [Prometheus](https://prometheus.io/) и
[Alertmanager](https://prometheus.io/docs/alerting/latest/alertmanager/) для приложения «Банк».

## Файлы

| Файл | Назначение |
|------|-----------|
| `prometheus.yml` | Глобальные настройки, scrape-конфиги микросервисов, подключение Alertmanager |
| `alert.rules.yml` | Правила алертов (доступность, HTTP, JVM, бизнес-метрики) |
| `alertmanager.yml` | Маршрутизация и группировка алертов |

## Сбор метрик

Метрики поставляются приложениями через **Spring Boot Actuator + Micrometer** по эндпоинту
`/actuator/prometheus`. Prometheus скрейпит все сервисы (job `bank-microservices`):

`front-ui:8080`, `gateway:8081`, `accounts-service:8082`, `cash-service:8083`,
`transfer-service:8084`, `notifications-service:8085`.

Собираются:
* HTTP-метрики — `http_server_requests_seconds_*` (RPS, 4xx, 5xx, персентили таймингов);
* JVM-метрики — `jvm_memory_*`, `process_cpu_usage`, `jvm_gc_*`, `jvm_threads_*`;
* кастомные бизнес-метрики:
  * `bank_cash_withdrawal_failed_total{login}` — неуспешные снятия;
  * `bank_transfer_failed_total{from_login,to_login}` — неуспешные переводы;
  * `bank_notification_send_failed_total{login}` — невозможность отправки уведомления.

## Алерты

Реализованы в Prometheus (`alert.rules.yml`), доставка через Alertmanager. Примеры порогов:

| Алерт | Условие |
|-------|---------|
| `ServiceDown` | сервис недоступен > 30s |
| `HighHttp5xxRate` | 5xx > 0.2 rps в течение 1m |
| `HighHttpLatencyP99` | p99 времени ответа > 1s |
| `HighJvmHeapUsage` | heap > 90% |
| `TooManyFailedWithdrawals` | > 3 неуспешных снятий за 5m |
| `TooManyFailedTransfers` | > 3 неуспешных переводов за 5m |
| `NotificationDeliveryFailing` | > 3 сбоев уведомлений за 5m |

## Доступ

* Prometheus UI: [http://localhost:9090](http://localhost:9090) (вкладки *Status → Targets*, *Alerts*).
* Alertmanager UI: [http://localhost:9093](http://localhost:9093).
