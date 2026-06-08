package ru.yandex.practicum.mybankfront.controller.dto;

public record CashRequestDto(String login, int value, CashAction action) {
}