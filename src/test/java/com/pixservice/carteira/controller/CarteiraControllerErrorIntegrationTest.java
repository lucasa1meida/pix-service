package com.pixservice.carteira.controller;

import com.pixservice.carteira.service.CarteiraService;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {
        "classpath:sql/setup.sql"
})
class CarteiraControllerErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarteiraService carteiraService;

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao criar carteira quando ocorrer erro inesperado")
    void deveRetornarInternalServerErrorAoCriarCarteiraErroInesperado() throws Exception {
        when(carteiraService.criar(anyString()))
                .thenThrow(new RuntimeException("Falha inesperada"));

        mockMvc.perform(post("/carteiras/{customerId}", "customer-erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message",
                        is("Erro ao salvar carteira: Falha inesperada")));
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao depositar quando ocorrer erro inesperado")
    void deveRetornarInternalServerErrorAoDepositarErroInesperado() throws Exception {
        UUID carteiraId = UUID.randomUUID();
        when(carteiraService.depositar(any(UUID.class), any()))
                .thenThrow(new RuntimeException("Falha inesperada no depósito"));

        mockMvc.perform(post("/carteiras/{carteiraId}/depositar", carteiraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 10.00}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message",
                        is("Erro ao depositar na carteira: Falha inesperada no depósito")));
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao sacar quando ocorrer erro inesperado")
    void deveRetornarInternalServerErrorAoSacarErroInesperado() throws Exception {
        UUID carteiraId = UUID.randomUUID();
        when(carteiraService.sacar(any(UUID.class), any()))
                .thenThrow(new RuntimeException("Falha inesperada no saque"));

        mockMvc.perform(post("/carteiras/{carteiraId}/sacar", carteiraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 10.00}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message",
                        is("Erro ao sacar na carteira: Falha inesperada no saque")));
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao obter saldo quando ocorrer erro inesperado")
    void deveRetornarInternalServerErrorAoObterSaldoErroInesperado() throws Exception {
        UUID carteiraId = UUID.randomUUID();
        when(carteiraService.obterSaldo(any(UUID.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Falha inesperada ao buscar saldo"));

        mockMvc.perform(get("/carteiras/{carteiraId}/saldo", carteiraId)
                        .param("data", "2025-01-01T10:00:00"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message",
                        is("Erro ao buscar carteira: Falha inesperada ao buscar saldo")));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST ao obter saldo quando ocorrer ExcecaoDeDominio")
    void deveRetornarBadRequestAoObterSaldoExcecaoDeDominio() throws Exception {
        UUID carteiraId = UUID.randomUUID();
        when(carteiraService.obterSaldo(any(UUID.class), any(LocalDateTime.class)))
                .thenThrow(new ExcecaoDeDominio("Erro de negócio"));

        mockMvc.perform(get("/carteiras/{carteiraId}/saldo", carteiraId)
                        .param("data", "2025-01-01T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Erro ao buscar carteira: Erro de negócio")));
    }
}


