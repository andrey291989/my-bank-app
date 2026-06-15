package ru.yandex.practicum.accounts.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.dto.AccountDto;
import ru.yandex.practicum.accounts.dto.AccountResponseDto;
import ru.yandex.practicum.accounts.dto.AccountUpdateRequestDto;
import ru.yandex.practicum.accounts.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{login}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable String login) {
        AccountResponseDto account = accountService.getAccount(login);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAllAccounts(@RequestParam(required = false) String currentLogin) {
        List<AccountDto> accounts = accountService.getAllAccountsExcept(currentLogin);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{login}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable String login,
            @Valid @RequestBody AccountUpdateRequestDto request
    ) {
        AccountResponseDto account = accountService.updateAccount(login, request);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{login}/balance")
    public ResponseEntity<AccountResponseDto> updateBalance(
            @PathVariable String login,
            @RequestParam BigDecimal delta
    ) {
        AccountResponseDto account = accountService.updateBalance(login, delta);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<AccountResponseDto> transfer(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount
    ) {
        AccountResponseDto account = accountService.transfer(from, to, amount);
        return ResponseEntity.ok(account);
    }
}