package ru.yandex.practicum.cash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cash.dto.AccountResponseDto;
import ru.yandex.practicum.cash.dto.CashRequestDto;
import ru.yandex.practicum.cash.dto.CashResponseDto;
import ru.yandex.practicum.cash.model.CashTransaction;
import ru.yandex.practicum.cash.repository.CashTransactionRepository;

@Service
public class CashService {

    @Autowired
    private AccountsClient accountsClient;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private CashTransactionRepository transactionRepository;

    @Transactional
    public CashResponseDto processCashOperation(CashRequestDto request) {
        int delta = request.action() == CashRequestDto.CashAction.PUT
                ? request.value()
                : -request.value();

        // Call Accounts service to update balance
        AccountResponseDto updatedAccount = accountsClient.updateBalance(request.login(), delta);

        // Record transaction
        int balanceBefore = request.action() == CashRequestDto.CashAction.PUT
                ? updatedAccount.sum() - request.value()
                : updatedAccount.sum() + request.value();

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
                ? "Deposit: %d rub added to your account".formatted(request.value())
                : "Withdrawal: %d rub withdrawn from your account".formatted(request.value());
        notificationClient.sendNotification(request.login(), message, "CASH_OPERATION");

        String responseMessage = request.action() == CashRequestDto.CashAction.PUT
                ? "Successfully deposited %d rub".formatted(request.value())
                : "Successfully withdrawn %d rub".formatted(request.value());

        return new CashResponseDto(
                updatedAccount.login(),
                updatedAccount.name(),
                updatedAccount.birthdate(),
                updatedAccount.sum(),
                responseMessage
        );
    }
}