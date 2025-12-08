package com.pixservice.pix.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtensionMethod({ExtensoesDeTeste.class})
class RegistroIdempotenciaTest {

    private static final String CHAVE_IDEMPOTENCIA = "123e4567-e89b-12d3-a456-426614174000";
    private static final String REFERENCIA = "E12345678202501151030123456789";
    private static final String PAYLOAD_HASH = "abc123def456";

    @Test
    @DisplayName("Deve instanciar RegistroIdempotencia com dados válidos")
    void deveInstanciarRegistroIdempotencia() {
        RegistroIdempotencia registro = new RegistroIdempotencia(CHAVE_IDEMPOTENCIA, REFERENCIA, PAYLOAD_HASH);

        assertNotNull(registro.getId());
        assertEquals(CHAVE_IDEMPOTENCIA, registro.getChaveIdempotencia());
        assertEquals(REFERENCIA, registro.getReferencia());
        assertEquals(PAYLOAD_HASH, registro.getPayloadHash());
        assertNotNull(registro.getDataDeCriacao());
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando chaveIdempotencia for nula ou vazia")
    void deveRetornarExcecaoCasoChaveIdempotenciaNulaOuVazia(String chaveIdempotencia) {
        assertThrows(ExcecaoDeDominio.class, () -> new RegistroIdempotencia(chaveIdempotencia, REFERENCIA, PAYLOAD_HASH))
                .comMensagemDeErro("chaveIdempotencia não pode ser nula ou vazia");
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando referencia for nula ou vazia")
    void deveRetornarExcecaoCasoReferenciaNulaOuVazia(String referencia) {
        assertThrows(ExcecaoDeDominio.class, () -> new RegistroIdempotencia(CHAVE_IDEMPOTENCIA, referencia, PAYLOAD_HASH))
                .comMensagemDeErro("referencia não pode ser nula ou vazia");
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando payloadHash for nulo ou vazio")
    void deveRetornarExcecaoCasoPayloadHashNuloOuVazio(String payloadHash) {
        assertThrows(ExcecaoDeDominio.class, () -> new RegistroIdempotencia(CHAVE_IDEMPOTENCIA, REFERENCIA, payloadHash))
                .comMensagemDeErro("payloadHash não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve criar registro com dataDeCriacao próxima ao momento atual")
    void deveCriarRegistroComDataDeCriacaoAtual() {
        LocalDateTime antes = LocalDateTime.now();
        RegistroIdempotencia registro = new RegistroIdempotencia(CHAVE_IDEMPOTENCIA, REFERENCIA, PAYLOAD_HASH);
        LocalDateTime depois = LocalDateTime.now();

        assertNotNull(registro.getDataDeCriacao());
        assertTrue(registro.getDataDeCriacao().isAfter(antes.minusSeconds(1)) || registro.getDataDeCriacao().isEqual(antes));
        assertTrue(registro.getDataDeCriacao().isBefore(depois.plusSeconds(1)) || registro.getDataDeCriacao().isEqual(depois));
    }
}

