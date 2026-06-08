package ru.yandex.practicum.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

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
                        .uri("lb://accounts-service"))

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
                        .uri("lb://cash-service"))

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
                        .uri("lb://transfer-service"))

                .build();
    }
}