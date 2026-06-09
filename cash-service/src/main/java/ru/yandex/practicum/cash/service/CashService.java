package ru.yandex.practicum.cash.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cash.dto.AccountResponseDto;
import ru.yandex.practicum.cash.dto.CashRequestDto;
import ru.yandex.practicum.cash.dto.CashResponseDto;
import ru.yandex.practicum.cash.model.CashTransaction;
import ru.yandex.practicum.cash.repository.CashTransactionRepository;

import java.math.BigDecimal;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final CashTransactionRepository transactionRepository;

    public CashService(AccountsClient accountsClient, NotificationClient notificationClient, CashTransactionRepository transactionRepository) {
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public CashResponseDto processCashOperation(CashRequestDto request) {
        BigDecimal delta = request.action() == CashRequestDto.CashAction.PUT
                ? request.value()
                : request.value().negate();

        // Call Accounts service to update balance
        AccountResponseDto updatedAccount = accountsClient.updateBalance(request.login(), delta);

        // Record transaction
        BigDecimal balanceBefore = request.action() == CashRequestDto.CashAction.PUT
                ? updatedAccount.sum().subtract(request.value())
                : updatedAccount.sum().add(request.value());

        CashTransaction transaction = new CashTransaction(
                request.login(),
                request.value(),
                request.action().toString(),
                balanceBefore,
                updatedAccount.sum()
        );
        transactionRepository.save(transaction);

        // Send notification
        String message = request.action() == CashRequestDto.CashAction.PUT
                ? "Deposit: %s rub added to your account".formatted(request.value())
                : "Withdrawal: %s rub withdrawn from your account".formatted(request.value());
        notificationClient.sendNotification(request.login(), message, "CASH_OPERATION");

        String responseMessage = request.action() == CashRequestDto.CashAction.PUT
                ? "Successfully deposited %s rub".formatted(request.value())
                : "Successfully withdrawn %s rub".formatted(request.value());

        return new CashResponseDto(
                updatedAccount.login(),
                updatedAccount.name(),
                updatedAccount.birthdate(),
                updatedAccount.sum(),
                responseMessage
        );
    }
}