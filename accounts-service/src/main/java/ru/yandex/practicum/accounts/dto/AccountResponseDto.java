package ru.yandex.practicum.accounts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountResponseDto(
        String login,
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthdate,
        BigDecimal sum
) {
}