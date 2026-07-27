package ru.yandex.practicum.transfer.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.transfer.dto.AccountResponseDto;
import ru.yandex.practicum.transfer.dto.TransferRequestDto;
import ru.yandex.practicum.transfer.dto.TransferResponseDto;
import ru.yandex.practicum.transfer.repository.TransferTransactionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTest {

    @Mock
    private AccountsClient accountsClient;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private TransferTransactionRepository transactionRepository;
    @Mock
    private TransferAuditService auditService;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferService = new TransferService(accountsClient, notificationClient,
                transactionRepository, auditService, meterRegistry);
    }

    @Test
    void processTransfer_shouldTransferAndNotifyBothSides() {
        var request = new TransferRequestDto("from", "to", new BigDecimal("300.00"));
        when(accountsClient.getAccount("from"))
                .thenReturn(new AccountResponseDto("from", "From", "1990-01-01", new BigDecimal("1000.00")));
        when(accountsClient.getAccount("to"))
                .thenReturn(new AccountResponseDto("to", "To", "1990-01-01", new BigDecimal("500.00")));
        when(accountsClient.transfer("from", "to", new BigDecimal("300.00")))
                .thenReturn(new AccountResponseDto("from", "From", "1990-01-01", new BigDecimal("700.00")));

        TransferResponseDto result = transferService.processTransfer(request);

        assertNotNull(result);
        assertEquals("from", result.fromLogin());
        assertEquals(new BigDecimal("700.00"), result.newBalance());
        verify(transactionRepository).save(any());
        // transfer владеет уведомлениями: отправителю и получателю
        verify(notificationClient).sendNotification(eq("from"), any(), eq("TRANSFER_OUT"));
        verify(notificationClient).sendNotification(eq("to"), any(), eq("TRANSFER_IN"));
    }

    @Test
    void processTransfer_shouldThrowWhenInsufficientFunds() {
        var request = new TransferRequestDto("from", "to", new BigDecimal("2000.00"));
        when(accountsClient.getAccount("from"))
                .thenReturn(new AccountResponseDto("from", "From", "1990-01-01", new BigDecimal("100.00")));

        // Без Spring-AOP fallback не вызывается — исключение пробрасывается напрямую
        assertThrows(RuntimeException.class, () -> transferService.processTransfer(request));
    }

    @Test
    void transferFallback_shouldCountMetricAndSaveFailedAudit() {
        var request = new TransferRequestDto("from", "to", new BigDecimal("300.00"));

        assertThrows(RuntimeException.class,
                () -> transferService.transferFallback(request, new RuntimeException("boom")));

        // Кастомная бизнес-метрика неуспешных переводов увеличилась
        assertEquals(1.0, meterRegistry.counter("bank.transfer.failed",
                "from_login", "from", "to_login", "to").count());
        // Неуспешная запись сохраняется в отдельной транзакции
        verify(auditService).saveFailedTransfer(eq(request), any());
    }
}
