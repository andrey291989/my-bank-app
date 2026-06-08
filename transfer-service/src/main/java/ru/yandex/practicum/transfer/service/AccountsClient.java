package ru.yandex.practicum.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.transfer.dto.AccountResponseDto;

@Service
public class AccountsClient {

    private static final Logger log = LoggerFactory.getLogger(AccountsClient.class);

    @Autowired
    @Qualifier("accountsWebClient")
    private WebClient accountsWebClient;

    public AccountResponseDto getAccount(String login) {
        try {
            return accountsWebClient.get()
                    .uri("/api/accounts/{login}", login)
                    .retrieve()
                    .bodyToMono(AccountResponseDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get account for {}: {}", login, e.getMessage());
            throw new RuntimeException("Failed to get account: " + e.getMessage(), e);
        }
    }

    public AccountResponseDto transfer(String fromLogin, String toLogin, int amount) {
        try {
            return accountsWebClient.post()
                    .uri("/api/accounts/transfer?from={from}&to={to}&amount={amount}", fromLogin, toLogin, amount)
                    .retrieve()
                    .bodyToMono(AccountResponseDto.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to transfer from {} to {}: {}", fromLogin, toLogin, e.getMessage());
            throw new RuntimeException("Failed to transfer: " + e.getMessage(), e);
        }
    }
}