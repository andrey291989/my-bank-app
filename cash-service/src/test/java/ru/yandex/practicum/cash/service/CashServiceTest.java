package ru.yandex.practicum.cash.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.cash.dto.AccountResponseDto;
import ru.yandex.practicum.cash.dto.CashRequestDto;
import ru.yandex.practicum.cash.dto.CashResponseDto;
import ru.yandex.practicum.cash.model.CashTransaction;
import ru.yandex.practicum.cash.repository.CashTransactionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CashServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private CashTransactionRepository transactionRepository;

    @InjectMocks
    private CashService cashService;

    private AccountResponseDto testAccountResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAccountResponse = new AccountResponseDto(
            "testuser",
            "Test User",
            "1990-01-01",
            new BigDecimal("1500.00")
        );
    }

    @Test
    void processCashOperation_ShouldProcessDeposit() {
        // Given
        CashRequestDto request = new CashRequestDto(
            "testuser",
            new BigDecimal("500.00"),
            CashRequestDto.CashAction.PUT
        );

        when(accountsClient.updateBalance("testuser", new BigDecimal("500.00"))).thenReturn(testAccountResponse);
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CashResponseDto result = cashService.processCashOperation(request);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Test User", result.name());
        assertEquals(new BigDecimal("1500.00"), result.sum());
        assertEquals("Successfully deposited 500.00 rub", result.message());

        verify(accountsClient).updateBalance("testuser", new BigDecimal("500.00"));
        verify(transactionRepository).save(any(CashTransaction.class));
        verify(notificationClient).sendNotification(
            "testuser",
            "Deposit: 500.00 rub added to your account",
            "CASH_OPERATION"
        );
    }

    @Test
    void processCashOperation_ShouldProcessWithdrawal() {
        // Given
        CashRequestDto request = new CashRequestDto(
            "testuser",
            new BigDecimal("300.00"),
            CashRequestDto.CashAction.GET
        );

        when(accountsClient.updateBalance("testuser", new BigDecimal("-300.00"))).thenReturn(testAccountResponse);
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CashResponseDto result = cashService.processCashOperation(request);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Test User", result.name());
        assertEquals(new BigDecimal("1500.00"), result.sum());
        assertEquals("Successfully withdrawn 300.00 rub", result.message());

        verify(accountsClient).updateBalance("testuser", new BigDecimal("-300.00"));
        verify(transactionRepository).save(any(CashTransaction.class));
        verify(notificationClient).sendNotification(
            "testuser",
            "Withdrawal: 300.00 rub withdrawn from your account",
            "CASH_OPERATION"
        );
    }
}