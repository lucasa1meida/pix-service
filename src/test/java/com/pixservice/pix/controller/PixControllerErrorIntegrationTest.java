package com.pixservice.pix.controller;

import com.pixservice.pix.dto.RequisicaoPixTransferenciaDTO;
import com.pixservice.pix.service.PixService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PixControllerErrorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PixService pixService;

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR ao transferir quando ocorrer erro inesperado")
    void deveRetornarInternalServerErrorAoTransferirErroInesperado() throws Exception {
        when(pixService.transferir(anyString(), any(UUID.class), anyString(), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Falha interna ao transferir"));

        RequisicaoPixTransferenciaDTO request = new RequisicaoPixTransferenciaDTO();
        request.setCarteiraIdOrigem(UUID.randomUUID());
        request.setChavePixDestino("destino-erro@example.com");
        request.setValor(new BigDecimal("10.00"));

        String body = """
                {
                  "carteiraIdOrigem": "%s",
                  "chavePixDestino": "%s",
                  "valor": 10.00
                }
                """.formatted(
                request.getCarteiraIdOrigem(),
                request.getChavePixDestino()
        );

        mockMvc.perform(post("/pix/transferencias")
                        .header("Idempotency-Key", "idempotency-key-erro-interno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Falha interna ao transferir")));
    }
}


