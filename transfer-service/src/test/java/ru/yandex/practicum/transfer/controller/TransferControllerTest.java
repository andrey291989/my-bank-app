package ru.yandex.practicum.transfer.controller;

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
import ru.yandex.practicum.transfer.config.SecurityConfig;
import ru.yandex.practicum.transfer.dto.TransferRequestDto;
import ru.yandex.practicum.transfer.dto.TransferResponseDto;
import ru.yandex.practicum.transfer.service.TransferService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransferController.class,
        excludeAutoConfiguration = OAuth2ClientAutoConfiguration.class)
@Import(SecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    void shouldReturn401WithoutJwt() throws Exception {
        var request = new TransferRequestDto("from", "to", new BigDecimal("100.00"));
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200ForValidRequestWithJwt() throws Exception {
        var request = new TransferRequestDto("from", "to", new BigDecimal("100.00"));
        when(transferService.processTransfer(any())).thenReturn(
                new TransferResponseDto("from", "From", "to", "To",
                        new BigDecimal("100.00"), new BigDecimal("900.00"), "ok"));

        mockMvc.perform(post("/api/transfer").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenFromLoginBlank() throws Exception {
        var request = new TransferRequestDto("", "to", new BigDecimal("100.00"));
        mockMvc.perform(post("/api/transfer").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAmountNotPositive() throws Exception {
        var request = new TransferRequestDto("from", "to", new BigDecimal("0.00"));
        mockMvc.perform(post("/api/transfer").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }
}
