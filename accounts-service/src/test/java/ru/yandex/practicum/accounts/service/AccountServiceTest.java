package ru.yandex.practicum.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.accounts.dto.AccountDto;
import ru.yandex.practicum.accounts.dto.AccountResponseDto;
import ru.yandex.practicum.accounts.dto.AccountUpdateRequestDto;
import ru.yandex.practicum.accounts.exceptions.AccountNotFoundException;
import ru.yandex.practicum.accounts.exceptions.InsufficientFundsException;
import ru.yandex.practicum.accounts.exceptions.InvalidTransferAmountException;
import ru.yandex.practicum.accounts.exceptions.UnderAgeException;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountResponseDto testAccountResponse;
    private AccountDto testAccountDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAccount = new Account(
            "testuser",
            "Test User",
            LocalDate.of(1990, 1, 1),
            new BigDecimal("1000.00")
        );

        testAccountResponse = new AccountResponseDto(
            "testuser",
            "Test User",
            LocalDate.of(1990, 1, 1),
            new BigDecimal("1000.00")
        );

        testAccountDto = new AccountDto(
            "testuser",
            "Test User"
        );
    }

    @Test
    void getAccount_ShouldReturnAccount_WhenAccountExists() {
        // Given
        when(accountRepository.findByLogin("testuser")).thenReturn(Optional.of(testAccount));

        // When
        AccountResponseDto result = accountService.getAccount("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Test User", result.name());
        assertEquals(new BigDecimal("1000.00"), result.sum());

        verify(accountRepository).findByLogin("testuser");
    }

    @Test
    void getAccount_ShouldThrowException_WhenAccountNotFound() {
        // Given
        when(accountRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccount("nonexistent");
        });

        assertEquals("nonexistent", exception.getMessage());
        verify(accountRepository).findByLogin("nonexistent");
    }

    @Test
    void getAllAccountsExcept_ShouldReturnListOfAccounts() {
        // Given
        List<AccountDto> expectedAccounts = Arrays.asList(testAccountDto);
        when(accountRepository.findAllAccountDtosExcept("currentuser")).thenReturn(expectedAccounts);

        // When
        List<AccountDto> result = accountService.getAllAccountsExcept("currentuser");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).login());
        assertEquals("Test User", result.get(0).name());

        verify(accountRepository).findAllAccountDtosExcept("currentuser");
    }

    @Test
    void updateAccount_ShouldUpdateAccount_WhenValidDataProvided() {
        // Given
        AccountUpdateRequestDto updateRequest = new AccountUpdateRequestDto(
            "Updated User",
            LocalDate.of(2000, 1, 1) // Over 18 years old
        );

        Account updatedAccount = new Account(
            "testuser",
            "Updated User",
            LocalDate.of(2000, 1, 1),
            new BigDecimal("1000.00")
        );

        when(accountRepository.findByLogin("testuser")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // When
        AccountResponseDto result = accountService.updateAccount("testuser", updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.login());
        assertEquals("Updated User", result.name());

        verify(accountRepository).findByLogin("testuser");
        verify(accountRepository).save(testAccount);
        verify(notificationClient).sendNotification(
            "testuser",
            "Your account information has been updated",
            "ACCOUNT_UPDATE"
        );
    }

    @Test
    void updateAccount_ShouldThrowException_WhenUserUnder18() {
        // Given
        AccountUpdateRequestDto updateRequest = new AccountUpdateRequestDto(
            "Young User",
            LocalDate.now().minusYears(10) // Under 18 years old
        );

        // When & Then
        UnderAgeException exception = assertThrows(UnderAgeException.class, () -> {
            accountService.updateAccount("testuser", updateRequest);
        });

        assertTrue(exception.getMessage().contains("User must be over 18 years old"));
        verify(accountRepository, never()).findByLogin(anyString());
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void updateBalance_ShouldIncreaseBalance_WhenPositiveDeltaProvided() {
        // Given
        when(accountRepository.findByLogin("testuser")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponseDto result = accountService.updateBalance("testuser", new BigDecimal("500.00"));

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.sum()); // 1000 + 500

        verify(accountRepository).findByLogin("testuser");
        verify(accountRepository).save(testAccount);
        verify(notificationClient).sendNotification(
            "testuser",
            "Your account has been credited with 500.00 rub",
            "BALANCE_CHANGE"
        );
    }

    @Test
    void updateBalance_ShouldDecreaseBalance_WhenNegativeDeltaProvided() {
        // Given
        when(accountRepository.findByLogin("testuser")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponseDto result = accountService.updateBalance("testuser", new BigDecimal("-300.00"));

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("700.00"), result.sum()); // 1000 - 300

        verify(accountRepository).findByLogin("testuser");
        verify(accountRepository).save(testAccount);
        verify(notificationClient).sendNotification(
            "testuser",
            "300.00 rub has been withdrawn from your account",
            "BALANCE_CHANGE"
        );
    }

    @Test
    void updateBalance_ShouldThrowException_WhenInsufficientFunds() {
        // Given
        when(accountRepository.findByLogin("testuser")).thenReturn(Optional.of(testAccount));

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            accountService.updateBalance("testuser", new BigDecimal("-1500.00")); // Trying to withdraw more than available
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        verify(accountRepository).findByLogin("testuser");
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void transfer_ShouldTransferAmountBetweenAccounts() {
        // Given
        Account fromAccount = new Account("fromuser", "From User", LocalDate.of(1990, 1, 1), new BigDecimal("1000.00"));
        Account toAccount = new Account("touser", "To User", LocalDate.of(1990, 1, 1), new BigDecimal("500.00"));

        when(accountRepository.findByLogin("fromuser")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByLogin("touser")).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(fromAccount).thenReturn(toAccount);

        // When
        AccountResponseDto result = accountService.transfer("fromuser", "touser", new BigDecimal("300.00"));

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("700.00"), fromAccount.getSum()); // 1000 - 300
        assertEquals(new BigDecimal("800.00"), toAccount.getSum()); // 500 + 300

        verify(accountRepository).findByLogin("fromuser");
        verify(accountRepository).findByLogin("touser");
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(notificationClient).sendNotification(
            "fromuser",
            "You have transferred 300.00 rub to touser",
            "TRANSFER_OUT"
        );
        verify(notificationClient).sendNotification(
            "touser",
            "You have received 300.00 rub from fromuser",
            "TRANSFER_IN"
        );
    }

    @Test
    void transfer_ShouldThrowException_WhenInvalidAmount() {
        // When & Then
        InvalidTransferAmountException exception = assertThrows(InvalidTransferAmountException.class, () -> {
            accountService.transfer("fromuser", "touser", new BigDecimal("-100.00"));
        });

        assertTrue(exception.getMessage().contains("Transfer amount must be positive"));
        verify(accountRepository, never()).findByLogin(anyString());
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void transfer_ShouldThrowException_WhenSourceAccountNotFound() {
        // Given
        when(accountRepository.findByLogin("fromuser")).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.transfer("fromuser", "touser", new BigDecimal("100.00"));
        });

        assertEquals("fromuser", exception.getMessage());
        verify(accountRepository).findByLogin("fromuser");
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void transfer_ShouldThrowException_WhenTargetAccountNotFound() {
        // Given
        when(accountRepository.findByLogin("fromuser")).thenReturn(Optional.of(testAccount));
        when(accountRepository.findByLogin("touser")).thenReturn(Optional.empty());

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            accountService.transfer("fromuser", "touser", new BigDecimal("100.00"));
        });

        assertEquals("touser", exception.getMessage());
        verify(accountRepository).findByLogin("fromuser");
        verify(accountRepository).findByLogin("touser");
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void transfer_ShouldThrowException_WhenInsufficientFunds() {
        // Given
        Account fromAccount = new Account("fromuser", "From User", LocalDate.of(1990, 1, 1), new BigDecimal("100.00"));
        Account toAccount = new Account("touser", "To User", LocalDate.of(1990, 1, 1), new BigDecimal("500.00"));

        when(accountRepository.findByLogin("fromuser")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByLogin("touser")).thenReturn(Optional.of(toAccount));

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            accountService.transfer("fromuser", "touser", new BigDecimal("200.00")); // Trying to transfer more than available
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        verify(accountRepository).findByLogin("fromuser");
        verify(accountRepository).findByLogin("touser");
        verify(accountRepository, never()).save(any(Account.class));
        verify(notificationClient, never()).sendNotification(anyString(), anyString(), anyString());
    }
}