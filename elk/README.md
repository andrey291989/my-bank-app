# ELK — сбор и анализ логов

Подпроект отвечает за развёртывание стека **Elasticsearch + Logstash + Kibana** для
сбора, хранения и анализа логов микросервисов и Front UI приложения «Банк».

## Поток логов

```
Микросервисы / Front UI  →  Logstash (TCP:5000, json_lines)  →  Elasticsearch  →  Kibana
        (logback + logstash-logback-encoder, единый JSON-формат)
```

* Все приложения логируют через **SLF4J + Logback** и отправляют логи
  `LogstashTcpSocketAppender`-ом в Logstash в едином JSON-формате
  (паттерн Microservice Chassis, см. `logback-spring.xml` в каждом модуле).
* В каждой записи присутствуют `traceId` и `spanId` (из Micrometer Tracing) — по ним
  логи в Kibana связываются с трейсами в Zipkin.

## Файлы

| Файл | Назначение |
|------|-----------|
| `logstash/pipeline/logstash.conf` | input (TCP json_lines) → filter → output (Elasticsearch) |
| `logstash/config/logstash.yml` | базовая конфигурация Logstash |

## Logstash

* **input** — TCP порт `5000`, кодек `json_lines`.
* **filter** — маскирование чувствительных данных:
  * пароли/секреты (`password`, `pwd`, `client-secret`, `Authorization: Bearer …`) → `****`;
  * номера счетов/карт (последовательности 12+ цифр) → маскируются, кроме последних 4;
  * гарантируется наличие полей `traceId` / `spanId`.
* **output** — Elasticsearch, индекс `bank-logs-YYYY.MM.dd`.

## Уровни логирования в приложениях

* `ERROR` — ошибки (например, неуспешная операция снятия/перевода, сбой доставки уведомления);
* `WARN` — предупреждения (неуспешные попытки бизнес-операций);
* `INFO` — ключевые операции (пополнение, снятие, перевод, доставка уведомления);
* `DEBUG` — диагностическая информация (`ru.yandex.practicum`).

## Kibana

UI: [http://localhost:5601](http://localhost:5601).

Первичная настройка:
1. **Stack Management → Data Views → Create data view**.
2. Name: `bank-logs`, index pattern: `bank-logs-*`, timestamp field: `@timestamp`.
3. Перейдите в **Discover** и фильтруйте логи по `service`, `level`, `traceId` и т. д.

Хранилище Elasticsearch — том Docker (`es_data`), single-node, без security (для среды разработки).
