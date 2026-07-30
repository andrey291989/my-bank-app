package ru.yandex.practicum.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    // Адреса бэкендов вынесены в настройки, чтобы одна и та же конфигурация работала
    // в двух режимах. Значение по умолчанию "lb://<service>" — балансировка через
    // service discovery (Consul), как при запуске через docker-compose. В Kubernetes
    // Consul не разворачивается, поэтому профиль kubernetes подставляет сюда прямые
    // адреса Service-ов (http://host:port), а роль discovery выполняет DNS кластера.
    // См. application-kubernetes.yml и charts/gateway/templates/deployment.yaml.
    @Value("${services.accounts.uri:lb://accounts-service}")
    private String accountsUri;

    @Value("${services.cash.uri:lb://cash-service}")
    private String cashUri;

    @Value("${services.transfer.uri:lb://transfer-service}")
    private String transferUri;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route to Accounts Service
                .route("accounts-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("accountsService")
                                        .setFallbackUri("forward:/fallback/accounts"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(accountsUri))

                // Route to Cash Service
                .route("cash-service", r -> r
                        .path("/api/cash/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("cashService")
                                        .setFallbackUri("forward:/fallback/cash"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(cashUri))

                // Route to Transfer Service
                .route("transfer-service", r -> r
                        .path("/api/transfer/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("transferService")
                                        .setFallbackUri("forward:/fallback/transfer"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)))
                        .uri(transferUri))

                .build();
    }
}