package ru.yandex.practicum.accounts.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    void account_ShouldBeCreatedWithConstructor() {
        // Given
        String login = "testuser";
        String name = "Test User";
        LocalDate birthdate = LocalDate.of(1990, 1, 1);
        BigDecimal sum = new BigDecimal("1000.00");

        // When
        Account account = new Account(login, name, birthdate, sum);

        // Then
        assertThat(account.getLogin()).isEqualTo(login);
        assertThat(account.getName()).isEqualTo(name);
        assertThat(account.getBirthdate()).isEqualTo(birthdate);
        assertThat(account.getSum()).isEqualTo(sum);
    }

    @Test
    void account_ShouldHaveDefaultConstructor() {
        // When
        Account account = new Account();

        // Then
        assertThat(account).isNotNull();
    }

    @Test
    void account_ShouldSetAndGetId() {
        // Given
        Account account = new Account();
        Long id = 1L;

        // When
        account.setId(id);

        // Then
        assertThat(account.getId()).isEqualTo(id);
    }

    @Test
    void account_ShouldSetAndGetLogin() {
        // Given
        Account account = new Account();
        String login = "testuser";

        // When
        account.setLogin(login);

        // Then
        assertThat(account.getLogin()).isEqualTo(login);
    }

    @Test
    void account_ShouldSetAndGetName() {
        // Given
        Account account = new Account();
        String name = "Test User";

        // When
        account.setName(name);

        // Then
        assertThat(account.getName()).isEqualTo(name);
    }

    @Test
    void account_ShouldSetAndGetBirthdate() {
        // Given
        Account account = new Account();
        LocalDate birthdate = LocalDate.of(1990, 1, 1);

        // When
        account.setBirthdate(birthdate);

        // Then
        assertThat(account.getBirthdate()).isEqualTo(birthdate);
    }

    @Test
    void account_ShouldSetAndGetSum() {
        // Given
        Account account = new Account();
        BigDecimal sum = new BigDecimal("1000.00");

        // When
        account.setSum(sum);

        // Then
        assertThat(account.getSum()).isEqualTo(sum);
    }

    @Test
    void account_ShouldSetAndGetCreatedAt() {
        // Given
        Account account = new Account();
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        account.setCreatedAt(createdAt);

        // Then
        assertThat(account.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void account_ShouldSetAndGetUpdatedAt() {
        // Given
        Account account = new Account();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        account.setUpdatedAt(updatedAt);

        // Then
        assertThat(account.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void onCreate_ShouldSetCreatedAt() {
        // Given
        Account account = new Account();

        // When
        account.onCreate();

        // Then
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void onUpdate_ShouldSetUpdatedAt() {
        // Given
        Account account = new Account();

        // When
        account.onUpdate();

        // Then
        assertThat(account.getUpdatedAt()).isNotNull();
    }
}