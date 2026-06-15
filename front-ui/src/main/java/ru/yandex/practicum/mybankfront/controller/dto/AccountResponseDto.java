package ru.yandex.practicum.mybankfront.controller.dto;

import java.math.BigDecimal;

public class AccountResponseDto {
    private String login;
    private String name;
    private String birthdate;
    private BigDecimal sum;

    public AccountResponseDto() {
    }

    public AccountResponseDto(String login, String name, String birthdate, BigDecimal sum) {
        this.login = login;
        this.name = name;
        this.birthdate = birthdate;
        this.sum = sum;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }
}