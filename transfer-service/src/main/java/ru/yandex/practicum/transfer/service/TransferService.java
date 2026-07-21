package ru.yandex.practicum.transfer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.transfer.dto.AccountResponseDto;
import ru.yandex.practicum.transfer.dto.TransferRequestDto;
import ru.yandex.practicum.transfer.dto.TransferResponseDto;
import ru.yandex.practicum.transfer.model.TransferTransaction;
import ru.yandex.practicum.transfer.repository.TransferTransactionRepository;

import java.math.BigDecimal;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final TransferTransactionRepository transactionRepository;
    private final MeterRegistry meterRegistry;

    public TransferService(AccountsClient accountsClient, NotificationClient notificationClient,
                           TransferTransactionRepository transactionRepository, MeterRegistry meterRegistry) {
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.transactionRepository = transactionRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    @Retry(name = "transferRetry", fallbackMethod = "transferFallback")
    @CircuitBreaker(name = "transferCircuitBreaker", fallbackMethod = "transferFallback")
    public TransferResponseDto processTransfer(TransferRequestDto request) {
        log.info("Начало перевода {} от '{}' к '{}'", request.amount(), request.fromLogin(), request.toLogin());
        // Get source account info before transfer
        AccountResponseDto fromAccountBefore = accountsClient.getAccount(request.fromLogin());

        if (fromAccountBefore.sum().compareTo(request.amount()) < 0) {
            throw new RuntimeException("Insufficient funds for transfer");
        }

        // Get target account info before transfer
        AccountResponseDto toAccountBefore = accountsClient.getAccount(request.toLogin());

        // Perform transfer
        AccountResponseDto fromAccountAfter = accountsClient.transfer(
                request.fromLogin(),
                request.toLogin(),
                request.amount()
        );

        // Record transaction
        TransferTransaction transaction = new TransferTransaction(
                request.fromLogin(),
                request.toLogin(),
                request.amount(),
                fromAccountBefore.sum(),
                fromAccountAfter.sum(),
                toAccountBefore.sum(),
                toAccountBefore.sum().add(request.amount()),
                "SUCCESS"
        );
        transactionRepository.save(transaction);

        // Send notifications
        notificationClient.sendNotification(
                request.fromLogin(),
                "You have transferred %s rub to %s".formatted(request.amount(), request.toLogin()),
                "TRANSFER_OUT"
        );
        notificationClient.sendNotification(
                request.toLogin(),
                "You have received %s rub from %s".formatted(request.amount(), request.fromLogin()),
                "TRANSFER_IN"
        );

        log.info("Перевод {} от '{}' к '{}' успешно выполнен, баланс отправителя: {}",
                request.amount(), request.fromLogin(), request.toLogin(), fromAccountAfter.sum());

        return new TransferResponseDto(
                fromAccountAfter.login(),
                fromAccountAfter.name(),
                request.toLogin(),
                toAccountBefore.name(),
                request.amount(),
                fromAccountAfter.sum(),
                "Transfer completed successfully"
        );
    }

    @SuppressWarnings("unused")
    private TransferResponseDto transferFallback(TransferRequestDto request, Throwable t) {
        // Кастомная бизнес-метрика: неуспешные попытки перевода (группировка по логинам отправителя и получателя)
        meterRegistry.counter("bank.transfer.failed",
                "from_login", request.fromLogin(),
                "to_login", request.toLogin()).increment();
        log.warn("Неуспешная попытка перевода {} от '{}' к '{}': {}",
                request.amount(), request.fromLogin(), request.toLogin(), t.getMessage());

        // Record failed transaction
        TransferTransaction transaction = new TransferTransaction(
                request.fromLogin(),
                request.toLogin(),
                request.amount(),
                null, null, null, null,
                "FAILED"
        );
        transaction.setErrorMessage(t.getMessage());
        transactionRepository.save(transaction);

        throw new RuntimeException("Transfer failed: " + t.getMessage(), t);
    }
}