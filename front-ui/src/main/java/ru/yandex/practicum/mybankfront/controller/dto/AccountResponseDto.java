package ru.yandex.practicum.mybankfront.controller.dto;

public class AccountResponseDto {
    private String login;
    private String name;
    private String birthdate;
    private Integer sum;

    public AccountResponseDto() {
    }

    public AccountResponseDto(String login, String name, String birthdate, Integer sum) {
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

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }
}