package ru.yandex.practicum.transfer.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.transfer.dto.AccountResponseDto;
import ru.yandex.practicum.transfer.dto.TransferRequestDto;
import ru.yandex.practicum.transfer.dto.TransferResponseDto;
import ru.yandex.practicum.transfer.model.TransferTransaction;
import ru.yandex.practicum.transfer.repository.TransferTransactionRepository;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    @Autowired
    private AccountsClient accountsClient;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private TransferTransactionRepository transactionRepository;

    @Transactional
    @Retry(name = "transferRetry", fallbackMethod = "transferFallback")
    @CircuitBreaker(name = "transferCircuitBreaker", fallbackMethod = "transferFallback")
    public TransferResponseDto processTransfer(TransferRequestDto request) {
        // Get source account info before transfer
        AccountResponseDto fromAccountBefore = accountsClient.getAccount(request.fromLogin());

        if (fromAccountBefore.sum() < request.amount()) {
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
                toAccountBefore.sum() + request.amount(),
                "SUCCESS"
        );
        transactionRepository.save(transaction);

        // Send notifications
        notificationClient.sendNotification(
                request.fromLogin(),
                "You have transferred %d rub to %s".formatted(request.amount(), request.toLogin()),
                "TRANSFER_OUT"
        );
        notificationClient.sendNotification(
                request.toLogin(),
                "You have received %d rub from %s".formatted(request.amount(), request.fromLogin()),
                "TRANSFER_IN"
        );

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
        log.error("Transfer failed for request {}: {}", request, t.getMessage());

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