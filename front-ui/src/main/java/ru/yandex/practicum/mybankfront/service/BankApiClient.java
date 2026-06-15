package ru.yandex.practicum.mybankfront.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountDto;
import ru.yandex.practicum.mybankfront.controller.dto.AccountResponseDto;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BankApiClient {

    private final WebClient webClient;
    private final String gatewayUrl;

    public BankApiClient(WebClient webClient, @Value("${gateway.url:http://localhost:8080}") String gatewayUrl) {
        this.webClient = webClient;
        this.gatewayUrl = gatewayUrl;
    }

    public AccountResponseDto getAccount(String login) {
        return webClient.get()
                .uri(gatewayUrl + "/api/accounts/{login}", login)
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }

    public List<AccountDto> getOtherAccounts(String currentLogin) {
        AccountDto[] accounts = webClient.get()
                .uri(gatewayUrl + "/api/accounts")
                .retrieve()
                .bodyToMono(AccountDto[].class)
                .block();

        if (accounts == null) return List.of();

        return List.of(accounts).stream()
                .filter(a -> !a.login().equals(currentLogin))
                .toList();
    }

    public AccountResponseDto updateAccount(String login, String name, LocalDate birthdate) {
        return webClient.put()
                .uri(gatewayUrl + "/api/accounts/{login}", login)
                .bodyValue(new AccountUpdateRequest(name, birthdate.format(DateTimeFormatter.ISO_DATE)))
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }

    public AccountResponseDto cashOperation(String login, BigDecimal value, CashAction action) {
        return webClient.post()
                .uri(gatewayUrl + "/api/cash")
                .bodyValue(new CashRequest(login, value, action))
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }

    public AccountResponseDto transfer(String fromLogin, String toLogin, BigDecimal value) {
        return webClient.post()
                .uri(gatewayUrl + "/api/transfer")
                .bodyValue(new TransferRequest(fromLogin, toLogin, value))
                .retrieve()
                .bodyToMono(AccountResponseDto.class)
                .block();
    }

    // Inner DTOs for requests
    private record AccountUpdateRequest(String name, String birthdate) {}
    private record CashRequest(String login, BigDecimal value, CashAction action) {}
    private record TransferRequest(String fromLogin, String toLogin, BigDecimal value) {}
}