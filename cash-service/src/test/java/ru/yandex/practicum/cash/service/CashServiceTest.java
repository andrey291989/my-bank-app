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
            1500
        );
    }

    @Test
    void processCashOperation_ShouldProcessDeposit() {
        // Given
        CashRequestDto request = new CashRequestDto(
            "testuser",
            500,
            CashRequestDto.CashAction.PUT
        );

        when(accountsClient.updateBalance("testuser", 500)).thenReturn(testAccountResponse);
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CashResponseDto result = cashService.processCashOperation(request);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Test User", result.name());
        assertEquals(1500, result.sum());
        assertEquals("Successfully deposited 500 rub", result.message());

        verify(accountsClient).updateBalance("testuser", 500);
        verify(transactionRepository).save(any(CashTransaction.class));
        verify(notificationClient).sendNotification(
            "testuser",
            "Deposit: 500 rub added to your account",
            "CASH_OPERATION"
        );
    }

    @Test
    void processCashOperation_ShouldProcessWithdrawal() {
        // Given
        CashRequestDto request = new CashRequestDto(
            "testuser",
            300,
            CashRequestDto.CashAction.GET
        );

        when(accountsClient.updateBalance("testuser", -300)).thenReturn(testAccountResponse);
        when(transactionRepository.save(any(CashTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CashResponseDto result = cashService.processCashOperation(request);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Test User", result.name());
        assertEquals(1500, result.sum());
        assertEquals("Successfully withdrawn 300 rub", result.message());

        verify(accountsClient).updateBalance("testuser", -300);
        verify(transactionRepository).save(any(CashTransaction.class));
        verify(notificationClient).sendNotification(
            "testuser",
            "Withdrawal: 300 rub withdrawn from your account",
            "CASH_OPERATION"
        );
    }
}