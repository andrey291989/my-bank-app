package ru.yandex.practicum.cash.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final CashTransactionRepository transactionRepository;
    private final MeterRegistry meterRegistry;

    public CashService(AccountsClient accountsClient, NotificationClient notificationClient,
                       CashTransactionRepository transactionRepository, MeterRegistry meterRegistry) {
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.transactionRepository = transactionRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public CashResponseDto processCashOperation(CashRequestDto request) {
        boolean isWithdrawal = request.action() == CashRequestDto.CashAction.GET;
        log.info("Начало операции {} на сумму {} для пользователя '{}'", request.action(), request.value(), request.login());

        try {
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

            log.info("Операция {} успешно выполнена для '{}', новый баланс: {}",
                    request.action(), request.login(), updatedAccount.sum());

            return new CashResponseDto(
                    updatedAccount.login(),
                    updatedAccount.name(),
                    updatedAccount.birthdate(),
                    updatedAccount.sum(),
                    responseMessage
            );
        } catch (Exception e) {
            if (isWithdrawal) {
                // Кастомная бизнес-метрика: неуспешные попытки снятия денег (группировка по логину)
                meterRegistry.counter("bank.cash.withdrawal.failed", "login", request.login()).increment();
                log.warn("Неуспешная попытка снятия {} для пользователя '{}': {}",
                        request.value(), request.login(), e.getMessage());
            } else {
                log.error("Ошибка операции пополнения для пользователя '{}': {}", request.login(), e.getMessage());
            }
            throw e;
        }
    }
}
