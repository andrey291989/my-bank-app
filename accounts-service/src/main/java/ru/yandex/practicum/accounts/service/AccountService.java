package ru.yandex.practicum.accounts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.dto.AccountDto;
import ru.yandex.practicum.accounts.dto.AccountResponseDto;
import ru.yandex.practicum.accounts.dto.AccountUpdateRequestDto;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private NotificationClient notificationClient;

    public AccountResponseDto getAccount(String login) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Account not found: " + login));

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
            throw new RuntimeException("User must be over 18 years old");
        }

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Account not found: " + login));

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
    public AccountResponseDto updateBalance(String login, int delta) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Account not found: " + login));

        int newSum = account.getSum() + delta;
        if (newSum < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setSum(newSum);
        account = accountRepository.save(account);

        String message = delta >= 0
                ? "Your account has been credited with %d rub".formatted(delta)
                : "%d rub has been withdrawn from your account".formatted(-delta);

        notificationClient.sendNotification(login, message, "BALANCE_CHANGE");

        return new AccountResponseDto(
                account.getLogin(),
                account.getName(),
                account.getBirthdate(),
                account.getSum()
        );
    }

    @Transactional
    public AccountResponseDto transfer(String fromLogin, String toLogin, int amount) {
        if (amount <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        Account fromAccount = accountRepository.findByLogin(fromLogin)
                .orElseThrow(() -> new RuntimeException("Source account not found: " + fromLogin));
        Account toAccount = accountRepository.findByLogin(toLogin)
                .orElseThrow(() -> new RuntimeException("Target account not found: " + toLogin));

        if (fromAccount.getSum() < amount) {
            throw new RuntimeException("Insufficient funds for transfer");
        }

        fromAccount.setSum(fromAccount.getSum() - amount);
        toAccount.setSum(toAccount.getSum() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        notificationClient.sendNotification(
                fromLogin,
                "You have transferred %d rub to %s".formatted(amount, toLogin),
                "TRANSFER_OUT"
        );
        notificationClient.sendNotification(
                toLogin,
                "You have received %d rub from %s".formatted(amount, fromLogin),
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