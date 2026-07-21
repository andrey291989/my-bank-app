package ru.yandex.practicum.mybankfront.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${gateway.url:http://localhost:8081}")
    private String gatewayUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Используем авто-конфигурированный WebClient.Builder (с ObservationWebClientCustomizer),
        // чтобы исходящие HTTP-запросы попадали в трейс и пробрасывали trace id / span id в заголовках.
        return builder
                .baseUrl(gatewayUrl)
                .build();
    }
}