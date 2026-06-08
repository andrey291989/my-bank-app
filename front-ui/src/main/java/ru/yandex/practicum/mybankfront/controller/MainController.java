package ru.yandex.practicum.mybankfront.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;
import ru.yandex.practicum.mybankfront.service.BankApiClient;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private BankApiClient bankApiClient;

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model, @AuthenticationPrincipal OidcUser user) {
        String login = user.getName();

        try {
            var account = bankApiClient.getAccount(login);
            var accounts = bankApiClient.getOtherAccounts(login);

            // Используем геттеры
            model.addAttribute("name", account.getName());
            model.addAttribute("birthdate", account.getBirthdate());
            model.addAttribute("sum", account.getSum());
            model.addAttribute("accounts", accounts);
        } catch (Exception e) {
            model.addAttribute("errors", List.of("Ошибка загрузки данных: " + e.getMessage()));
        }

        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @AuthenticationPrincipal OidcUser user,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate
    ) {
        String login = user.getName();

        try {
            var account = bankApiClient.updateAccount(login, name, birthdate);
            var accounts = bankApiClient.getOtherAccounts(login);

            model.addAttribute("name", account.getName());
            model.addAttribute("birthdate", account.getBirthdate());
            model.addAttribute("sum", account.getSum());
            model.addAttribute("accounts", accounts);
            model.addAttribute("info", "Данные успешно обновлены");
        } catch (Exception e) {
            model.addAttribute("errors", List.of("Ошибка обновления: " + e.getMessage()));
            return getAccount(model, user);
        }

        return "main";
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            @AuthenticationPrincipal OidcUser user,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action
    ) {
        String login = user.getName();

        try {
            var account = bankApiClient.cashOperation(login, value, action);
            var accounts = bankApiClient.getOtherAccounts(login);

            model.addAttribute("name", account.getName());
            model.addAttribute("birthdate", account.getBirthdate());
            model.addAttribute("sum", account.getSum());
            model.addAttribute("accounts", accounts);

            String message = action == CashAction.GET
                    ? "Снято %d руб".formatted(value)
                    : "Положено %d руб".formatted(value);
            model.addAttribute("info", message);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            return getAccount(model, user);
        }

        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @AuthenticationPrincipal OidcUser user,
            @RequestParam("value") int value,
            @RequestParam("login") String targetLogin
    ) {
        String sourceLogin = user.getName();

        try {
            var account = bankApiClient.transfer(sourceLogin, targetLogin, value);
            var accounts = bankApiClient.getOtherAccounts(sourceLogin);

            model.addAttribute("name", account.getName());
            model.addAttribute("birthdate", account.getBirthdate());
            model.addAttribute("sum", account.getSum());
            model.addAttribute("accounts", accounts);
            model.addAttribute("info", "Успешно переведено %d руб клиенту".formatted(value));
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            return getAccount(model, user);
        }

        return "main";
    }
}