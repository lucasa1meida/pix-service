package com.pixservice.carteira.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixservice.carteira.dto.MovimentacaoCarteiraDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {
        "classpath:sql/setup.sql",
        "classpath:sql/datasets/carteira/carteira-controller-integration-test.sql"
})
class CarteiraControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID CARTEIRA_ID_FIXA =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    @DisplayName("Deve criar carteira com sucesso")
    void deveCriarCarteiraComSucesso() throws Exception {
        String customerId = "novo-customer";

        mockMvc.perform(post("/carteiras/{customerId}", customerId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carteiraId", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customerId)))
                .andExpect(jsonPath("$.saldo", is(0)))
                .andExpect(jsonPath("$.dataDeCriacao", notNullValue()))
                .andExpect(jsonPath("$.dataDeAtualizacao", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST ao criar carteira com customerId em branco")
    void deveRetornarBadRequestAoCriarCarteiraComCustomerVazio() throws Exception {
        String customerId = "   ";

        mockMvc.perform(post("/carteiras/{customerId}", customerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Erro ao salvar carteira: customerId não pode ser nulo ou vazio")));
    }

    @Test
    @DisplayName("Deve depositar na carteira e atualizar saldo")
    void deveDepositarNaCarteira() throws Exception {
        MovimentacaoCarteiraDTO request = new MovimentacaoCarteiraDTO();
        request.setQuantidade(new BigDecimal("50.00"));

        mockMvc.perform(post("/carteiras/{carteiraId}/depositar", CARTEIRA_ID_FIXA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carteiraId", is(CARTEIRA_ID_FIXA.toString())))
                .andExpect(jsonPath("$.customerId", is("customer-teste")))
                .andExpect(jsonPath("$.saldo", is(150.0)))
                .andExpect(jsonPath("$.dataDeCriacao", notNullValue()))
                .andExpect(jsonPath("$.dataDeAtualizacao", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST ao depositar quantidade inválida")
    void deveRetornarBadRequestAoDepositarQuantidadeInvalida() throws Exception {
        MovimentacaoCarteiraDTO request = new MovimentacaoCarteiraDTO();
        request.setQuantidade(new BigDecimal("0.00"));

        mockMvc.perform(post("/carteiras/{carteiraId}/depositar", CARTEIRA_ID_FIXA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Erro ao depositar na carteira: Quantidade a depositar deve ser positiva.")));
    }

    @Test
    @DisplayName("Deve sacar da carteira e atualizar saldo")
    void deveSacarDaCarteira() throws Exception {
        MovimentacaoCarteiraDTO request = new MovimentacaoCarteiraDTO();
        request.setQuantidade(new BigDecimal("50.00"));

        mockMvc.perform(post("/carteiras/{carteiraId}/sacar", CARTEIRA_ID_FIXA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carteiraId", is(CARTEIRA_ID_FIXA.toString())))
                .andExpect(jsonPath("$.customerId", is("customer-teste")))
                .andExpect(jsonPath("$.saldo", is(50.0)))
                .andExpect(jsonPath("$.dataDeCriacao", notNullValue()))
                .andExpect(jsonPath("$.dataDeAtualizacao", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST ao sacar com saldo insuficiente")
    void deveRetornarBadRequestAoSacarComSaldoInsuficiente() throws Exception {
        MovimentacaoCarteiraDTO request = new MovimentacaoCarteiraDTO();
        request.setQuantidade(new BigDecimal("200.00"));

        mockMvc.perform(post("/carteiras/{carteiraId}/sacar", CARTEIRA_ID_FIXA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Erro ao sacar na carteira: Fundos insuficientes.")));
    }

    @Test
    @DisplayName("Deve obter saldo atual quando data não for informada")
    void deveObterSaldoAtualQuandoDataForNula() throws Exception {
        mockMvc.perform(get("/carteiras/{carteiraId}/saldo", CARTEIRA_ID_FIXA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(100.0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND ao buscar saldo histórico sem transações")
    void deveRetornarNotFoundQuandoHistoricoNaoForEncontrado() throws Exception {
        UUID carteiraSemHistorico = UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(get("/carteiras/{carteiraId}/saldo", carteiraSemHistorico)
                        .param("data", "2025-01-01T10:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        is("Dados não encontrados: Transação não encontrada.")));
    }

    @Test
    @DisplayName("Deve obter saldo histórico quando data for informada")
    void deveObterSaldoHistoricoQuandoDataForInformada() throws Exception {
        mockMvc.perform(get("/carteiras/{carteiraId}/saldo", CARTEIRA_ID_FIXA)
                        .param("data", "2025-01-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(150.0)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }
}


