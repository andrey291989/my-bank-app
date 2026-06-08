package ru.yandex.practicum.accounts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.accounts.model.Account;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByLogin(String login);

    boolean existsByLogin(String login);

    @Query("SELECT a FROM Account a WHERE a.login != :currentLogin")
    List<Account> findAllExcept(@Param("currentLogin") String currentLogin);

    @Query("SELECT new ru.yandex.practicum.accounts.dto.AccountDto(a.login, a.name) FROM Account a WHERE a.login != :currentLogin")
    List<ru.yandex.practicum.accounts.dto.AccountDto> findAllAccountDtosExcept(@Param("currentLogin") String currentLogin);
}