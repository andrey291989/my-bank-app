package ru.yandex.practicum.cash.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.config.SecurityConfig;
import ru.yandex.practicum.cash.dto.CashRequestDto;
import ru.yandex.practicum.cash.dto.CashResponseDto;
import ru.yandex.practicum.cash.service.CashService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CashController.class,
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(SecurityConfig.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CashService cashService;

    // Требуется resource-server-ом для создания JwtDecoder без обращения к Keycloak
    @MockBean
    private JwtDecoder jwtDecoder;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    void shouldReturn401WithoutJwt() throws Exception {
        var request = new CashRequestDto("user", new BigDecimal("100.00"), CashRequestDto.CashAction.PUT);
        mockMvc.perform(post("/api/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200ForValidRequestWithJwt() throws Exception {
        var request = new CashRequestDto("user", new BigDecimal("100.00"), CashRequestDto.CashAction.PUT);
        when(cashService.processCashOperation(any()))
                .thenReturn(new CashResponseDto("user", "User", "1990-01-01", new BigDecimal("100.00"), "ok"));

        mockMvc.perform(post("/api/cash").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenLoginBlank() throws Exception {
        var request = new CashRequestDto("", new BigDecimal("100.00"), CashRequestDto.CashAction.PUT);
        mockMvc.perform(post("/api/cash").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAmountNotPositive() throws Exception {
        var request = new CashRequestDto("user", new BigDecimal("0.00"), CashRequestDto.CashAction.PUT);
        mockMvc.perform(post("/api/cash").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenActionNull() throws Exception {
        var request = new CashRequestDto("user", new BigDecimal("100.00"), null);
        mockMvc.perform(post("/api/cash").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }
}
