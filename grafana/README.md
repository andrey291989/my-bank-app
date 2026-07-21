# Grafana — визуализация метрик

Подпроект содержит provisioning-конфигурацию [Grafana](https://grafana.com/): источник
данных и дашборды, которые применяются автоматически при старте контейнера.

## Структура

```
grafana/
├── provisioning/
│   ├── datasources/datasource.yml   # источник данных Prometheus (uid: prometheus)
│   └── dashboards/dashboards.yml    # провайдер, загружает дашборды из /var/lib/grafana/dashboards
└── dashboards/
    ├── http-metrics.json            # RPS, 4xx, 5xx, персентили таймингов
    ├── jvm-metrics.json             # память, CPU, потоки, GC
    └── business-metrics.json        # неуспешные снятия/переводы/уведомления
```

## Источник данных

Prometheus (`http://prometheus:9090`) настроен как источник по умолчанию с
`uid: prometheus` — на него ссылаются все панели дашбордов.

## Дашборды

| Дашборд | Содержимое |
|---------|-----------|
| **Bank — HTTP метрики** | RPS по сервисам, 4xx, 5xx, персентили p50/p95/p99 |
| **Bank — JVM метрики** | heap/non-heap память, CPU (process/system), потоки, паузы GC |
| **Bank — бизнес-метрики** | неуспешные снятия (по логину), неуспешные переводы (отправитель→получатель), сбои уведомлений (по логину) |

Дашборды содержат переменную `application` для фильтрации по сервису.

> При необходимости можно импортировать готовые community-дашборды, например
> *JVM (Micrometer)* — ID `4701`, *Spring Boot Statistics* — ID `6756`.

## Алерты

Пороговые алерты в проекте настроены на стороне **Prometheus/Alertmanager**
(см. подпроект `prometheus`). Grafana используется для визуализации; при желании
на её панелях можно дополнительно завести Grafana-алерты.

## Доступ

Grafana UI: [http://localhost:3000](http://localhost:3000) (логин/пароль по умолчанию `admin` / `admin`).
Дашборды — в папке **Bank**.
