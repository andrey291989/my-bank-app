# Zipkin — распределённая трассировка

Подпроект отвечает за развёртывание [Zipkin](https://zipkin.io/) — системы сбора и
визуализации распределённых трейсов приложения «Банк».

## Как это работает

* Микросервисы и Front UI поставляют трейсы через **Micrometer Tracing** (bridge Brave)
  и `zipkin-reporter-brave`.
* Трейсы отправляются HTTP-репортёром на эндпоинт `POST /api/v2/spans`.
* Front UI генерирует новый `trace id` для входящего пользовательского запроса; при
  исходящих HTTP-запросах `trace id` и родительский `span id` пробрасываются в
  заголовках (форматы B3 / W3C `traceparent`).
* Дочерние спаны создаются автоматически для исходящих HTTP-запросов, запросов в БД
  (через `datasource-micrometer`) и в Apache Kafka (observation у `KafkaTemplate`
  и listener-контейнера).

## Конфигурация

Zipkin запускается контейнером `openzipkin/zipkin` (см. корневой `docker-compose.yml`).
Хранилище — **in-memory** (по умолчанию), данные не персистятся между перезапусками.

| Параметр | Значение |
|----------|----------|
| Порт UI / API | `9411` |
| Эндпоинт приёма | `http://zipkin:9411/api/v2/spans` |
| Хранилище | `mem` (in-memory) |

Приложения получают адрес Zipkin через переменную окружения
`ZIPKIN_ENDPOINT` (по умолчанию `http://localhost:9411/api/v2/spans`), которая
подставляется в `management.zipkin.tracing.endpoint`.

## Доступ

UI: [http://localhost:9411](http://localhost:9411)

Найдите трейс по имени сервиса (`serviceName`), затем разверните спаны, чтобы увидеть
проход запроса через `front-ui → gateway → accounts/cash/transfer → БД / Kafka →
notifications`. `trace id` совпадает с полем `traceId` в логах Kibana.
