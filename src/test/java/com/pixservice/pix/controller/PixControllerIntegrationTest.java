package com.pixservice.pix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixservice.pix.dto.RegistroChavePixDTO;
import com.pixservice.pix.dto.RequisicaoPixTransferenciaDTO;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.enums.TipoChavePix;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {
        "classpath:sql/setup.sql",
        "classpath:sql/datasets/pix/pix-controller-integration-test.sql"
})
class PixControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID CARTEIRA_ORIGEM_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID CARTEIRA_DESTINO_ID =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    @DisplayName("Deve registrar chave Pix com sucesso")
    void deveRegistrarChavePixComSucesso() throws Exception {
        RegistroChavePixDTO request = new RegistroChavePixDTO();
        request.setTipoChavePix(TipoChavePix.EMAIL);
        request.setValorChave("novo-usuario@example.com");

        mockMvc.perform(post("/carteiras/{carteiraId}/chaves-pix", CARTEIRA_ORIGEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.carteiraId", is(CARTEIRA_ORIGEM_ID.toString())))
                .andExpect(jsonPath("$.tipoChavePix", is("EMAIL")))
                .andExpect(jsonPath("$.valorChave", is("novo-usuario@example.com")))
                .andExpect(jsonPath("$.ativo", is(true)));
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao registrar chave Pix para carteira inexistente")
    void deveRetornarErroAoRegistrarChavePixQuandoCarteiraNaoExiste() throws Exception {
        RegistroChavePixDTO request = new RegistroChavePixDTO();
        request.setTipoChavePix(TipoChavePix.EMAIL);
        request.setValorChave("erro-usuario@example.com");

        UUID carteiraInexistente = UUID.fromString("99999999-9999-9999-9999-999999999999");

        mockMvc.perform(post("/carteiras/{carteiraId}/chaves-pix", carteiraInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Erro: Carteira não encontrada.")));
    }

    @Test
    @DisplayName("Deve realizar transferência Pix com sucesso")
    void deveTransferirPixComSucesso() throws Exception {
        RequisicaoPixTransferenciaDTO request = new RequisicaoPixTransferenciaDTO();
        request.setCarteiraIdOrigem(CARTEIRA_ORIGEM_ID);
        request.setChavePixDestino("destino@example.com");
        request.setValor(new BigDecimal("100.00"));

        String idempotencyKey = "idempotency-key-integration-test";

        mockMvc.perform(post("/pix/transferencias")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.referencia", notNullValue()))
                .andExpect(jsonPath("$.carteiraIdOrigem", is(CARTEIRA_DESTINO_ID.toString())))
                .andExpect(jsonPath("$.carteiraIdDestino", is(CARTEIRA_ORIGEM_ID.toString())))
                .andExpect(jsonPath("$.chavePix", is("destino@example.com")))
                .andExpect(jsonPath("$.valor", is(100.0)))
                .andExpect(jsonPath("$.status", is("PENDENTE")));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST quando carteira de origem não existir")
    void deveRetornarBadRequestQuandoCarteiraOrigemNaoExistir() throws Exception {
        RequisicaoPixTransferenciaDTO request = new RequisicaoPixTransferenciaDTO();
        request.setCarteiraIdOrigem(UUID.fromString("99999999-9999-9999-9999-999999999999"));
        request.setChavePixDestino("destino@example.com");
        request.setValor(new BigDecimal("50.00"));

        String idempotencyKey = "idempotency-key-integration-test-erro";

        mockMvc.perform(post("/pix/transferencias")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("Deve processar webhook CONFIRMADO atualizando transferência para CONFIRMADO")
    void deveProcessarWebhookConfirmadoComSucesso() throws Exception {
        RequisicaoPixTransferenciaDTO transferenciaRequest = new RequisicaoPixTransferenciaDTO();
        transferenciaRequest.setCarteiraIdOrigem(CARTEIRA_ORIGEM_ID);
        transferenciaRequest.setChavePixDestino("destino@example.com");
        transferenciaRequest.setValor(new BigDecimal("100.00"));

        String idempotencyKey = "idempotency-key-webhook-success";

        MvcResult transferenciaResult = mockMvc.perform(post("/pix/transferencias")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferenciaRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String transferenciaJson = transferenciaResult.getResponse().getContentAsString();
        String referencia = objectMapper.readTree(transferenciaJson).get("referencia").asText();

        RequisicaoPixWebhookDTO webhookRequest = new RequisicaoPixWebhookDTO();
        webhookRequest.setReferencia(referencia);
        webhookRequest.setEventoId("evt-" + UUID.randomUUID());
        webhookRequest.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        webhookRequest.setDataDeOcorrencia(LocalDateTime.now());

        mockMvc.perform(post("/pix/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencia", is(referencia)))
                .andExpect(jsonPath("$.status", is("CONFIRMADO")));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST ao processar webhook quando transferência não for encontrada")
    void deveRetornarBadRequestQuandoTransferenciaNaoForEncontradaNoWebhook() throws Exception {
        RequisicaoPixWebhookDTO webhookRequest = new RequisicaoPixWebhookDTO();
        String referenciaInexistente = "E00000000000000000000000000000000";
        webhookRequest.setReferencia(referenciaInexistente);
        webhookRequest.setEventoId("evt-" + UUID.randomUUID());
        webhookRequest.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        webhookRequest.setDataDeOcorrencia(LocalDateTime.now());

        mockMvc.perform(post("/pix/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Transferência Pix não encontrada para a referência " + referenciaInexistente)));
    }
}