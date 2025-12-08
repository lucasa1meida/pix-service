package com.pixservice.pix.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtensionMethod({ExtensoesDeTeste.class})
class EventoPixWebhookTest {

    private static final String EVENTO_ID = "evt-123e4567-e89b-12d3-a456-426614174000";
    private static final String END_TO_END_ID = "E12345678202501151030123456789";
    private static final TipoEventoPixWebhook TIPO = TipoEventoPixWebhook.CONFIRMADO;
    private static final LocalDateTime DATA_OCORRENCIA = LocalDateTime.now();
    private static final LocalDateTime DATA_RECEBIMENTO = LocalDateTime.now();

    @Test
    @DisplayName("Deve instanciar EventoPixWebhook com dados válidos")
    void deveInstanciarEventoPixWebhook() {
        EventoPixWebhook evento = new EventoPixWebhook(EVENTO_ID, END_TO_END_ID, TIPO,
                DATA_OCORRENCIA, DATA_RECEBIMENTO);

        assertNotNull(evento.getId());
        assertEquals(EVENTO_ID, evento.getEventoId());
        assertEquals(END_TO_END_ID, evento.getEndToEndId());
        assertEquals(TIPO, evento.getTipo());
        assertEquals(DATA_OCORRENCIA, evento.getDataDeOcorrencia());
        assertEquals(DATA_RECEBIMENTO, evento.getDataDeRecebimento());
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando eventoId for nulo ou vazio")
    void deveRetornarExcecaoCasoEventoIdNuloOuVazio(String eventoId) {
        assertThrows(ExcecaoDeDominio.class, () -> new EventoPixWebhook(eventoId, END_TO_END_ID, TIPO,
                DATA_OCORRENCIA, DATA_RECEBIMENTO))
                .comMensagemDeErro("eventoId não pode ser nulo ou vazio");
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando endToEndId for nulo ou vazio")
    void deveRetornarExcecaoCasoEndToEndIdNuloOuVazio(String endToEndId) {
        assertThrows(ExcecaoDeDominio.class, () -> new EventoPixWebhook(EVENTO_ID, endToEndId, TIPO,
                DATA_OCORRENCIA, DATA_RECEBIMENTO))
                .comMensagemDeErro("endToEndId não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando tipo do evento for nulo")
    void deveRetornarExcecaoCasoTipoNulo() {
        assertThrows(ExcecaoDeDominio.class, () -> new EventoPixWebhook(EVENTO_ID, END_TO_END_ID, null,
                DATA_OCORRENCIA, DATA_RECEBIMENTO))
                .comMensagemDeErro("tipo não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando dataDeOcorrencia for nula")
    void deveRetornarExcecaoCasoDataDeOcorrenciaNula() {
        assertThrows(ExcecaoDeDominio.class, () -> new EventoPixWebhook(EVENTO_ID, END_TO_END_ID, TIPO,
                null, DATA_RECEBIMENTO))
                .comMensagemDeErro("dataDeOcorrencia não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando dataDeRecebimento for nula")
    void deveRetornarExcecaoCasoDataDeRecebimentoNula() {
        assertThrows(ExcecaoDeDominio.class, () -> new EventoPixWebhook(EVENTO_ID, END_TO_END_ID, TIPO,
                DATA_OCORRENCIA, null))
                .comMensagemDeErro("dataDeRecebimento não pode ser nula");
    }

    @Test
    @DisplayName("Deve instanciar evento com tipo CONFIRMADO")
    void deveInstanciarEventoComTipoConfirmado() {
        EventoPixWebhook evento = new EventoPixWebhook(EVENTO_ID, END_TO_END_ID,
                TipoEventoPixWebhook.CONFIRMADO, DATA_OCORRENCIA, DATA_RECEBIMENTO);

        assertEquals(TipoEventoPixWebhook.CONFIRMADO, evento.getTipo());
    }

    @Test
    @DisplayName("Deve instanciar evento com tipo REJEITADO")
    void deveInstanciarEventoComTipoRejeitado() {
        EventoPixWebhook evento = new EventoPixWebhook(EVENTO_ID, END_TO_END_ID,
                TipoEventoPixWebhook.REJEITADO, DATA_OCORRENCIA, DATA_RECEBIMENTO);

        assertEquals(TipoEventoPixWebhook.REJEITADO, evento.getTipo());
    }
}

