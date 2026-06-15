package ru.yandex.practicum.accounts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AccountUpdateRequestDto(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthdate
) {
}