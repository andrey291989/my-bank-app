package ru.yandex.practicum.cash.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.cash.dto.CashRequestDto;
import ru.yandex.practicum.cash.dto.CashResponseDto;
import ru.yandex.practicum.cash.service.CashService;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    @Autowired
    private CashService cashService;

    @PostMapping
    public ResponseEntity<CashResponseDto> processCash(@Valid @RequestBody CashRequestDto request) {
        CashResponseDto response = cashService.processCashOperation(request);
        return ResponseEntity.ok(response);
    }
}