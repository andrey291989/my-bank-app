package ru.yandex.practicum.accounts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.Period;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final NotificationClient notificationClient;

    public AccountService(AccountRepository accountRepository, NotificationClient notificationClient) {
        this.accountRepository = accountRepository;
        this.notificationClient = notificationClient;
    }

    public AccountResponseDto getAccount(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        return new AccountResponseDto(
                account.getLogin(),
                account.getName(),
                account.getBirthdate(),
                account.getSum()
        );
    }

    public List<AccountDto> getAllAccountsExcept(String currentLogin) {
        return accountRepository.findAllAccountDtosExcept(currentLogin);
    }

    @Transactional
    public AccountResponseDto updateAccount(String login, AccountUpdateRequestDto request) {
        // Validate age (must be over 18)
        LocalDate birthdate = request.birthdate();
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new UnderAgeException(age);
        }

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        account.setName(request.name());
        account.setBirthdate(birthdate);
        account = accountRepository.save(account);

        // Send notification
        notificationClient.sendNotification(
                login,
                "Your account information has been updated",
                "ACCOUNT_UPDATE"
        );

        return new AccountResponseDto(
                account.getLogin(),
                account.getName(),
                account.getBirthdate(),
                account.getSum()
        );
    }

    @Transactional
    public AccountResponseDto updateBalance(String login, BigDecimal delta) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));

        BigDecimal newSum = account.getSum().add(delta);
        if (newSum.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(account.getSum(), delta.abs());
        }

        account.setSum(newSum);
        account = accountRepository.save(account);

        String message = delta.compareTo(BigDecimal.ZERO) >= 0
                ? "Your account has been credited with %s rub".formatted(delta)
                : "%s rub has been withdrawn from your account".formatted(delta.abs());

        notificationClient.sendNotification(login, message, "BALANCE_CHANGE");

        return new AccountResponseDto(
                account.getLogin(),
                account.getName(),
                account.getBirthdate(),
                account.getSum()
        );
    }

    @Transactional
    public AccountResponseDto transfer(String fromLogin, String toLogin, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmountException(amount);
        }

        Account fromAccount = accountRepository.findByLogin(fromLogin)
                .orElseThrow(() -> new AccountNotFoundException(fromLogin));
        Account toAccount = accountRepository.findByLogin(toLogin)
                .orElseThrow(() -> new AccountNotFoundException(toLogin));

        if (fromAccount.getSum().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromAccount.getSum(), amount);
        }

        fromAccount.setSum(fromAccount.getSum().subtract(amount));
        toAccount.setSum(toAccount.getSum().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        notificationClient.sendNotification(
                fromLogin,
                "You have transferred %s rub to %s".formatted(amount, toLogin),
                "TRANSFER_OUT"
        );
        notificationClient.sendNotification(
                toLogin,
                "You have received %s rub from %s".formatted(amount, fromLogin),
                "TRANSFER_IN"
        );

        return new AccountResponseDto(
                fromAccount.getLogin(),
                fromAccount.getName(),
                fromAccount.getBirthdate(),
                fromAccount.getSum()
        );
    }
}