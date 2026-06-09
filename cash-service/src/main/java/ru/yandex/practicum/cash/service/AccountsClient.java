package ru.yandex.practicum.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.cash.dto.AccountResponseDto;

import java.math.BigDecimal;

@Service
public class AccountsClient {

    private static final Logger log = LoggerFactory.getLogger(AccountsClient.class);

    private final WebClient accountsWebClient;

    public AccountsClient(@Qualifier("accountsWebClient") WebClient accountsWebClient) {
        this.accountsWebClient = accountsWebClient;
    }

    public AccountResponseDto updateBalance(String login, BigDecimal delta) {
        try {
            return accountsWebClient.post()
                    .uri("/api/accounts/{login}/balance?delta={delta}", login, delta)
                    .retrieve()
                    .bodyToMono(AccountResponseDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to update balance for {}: {}", login, e.getMessage());
            throw new RuntimeException("Failed to update balance: " + e.getMessage(), e);
        }
    }
}