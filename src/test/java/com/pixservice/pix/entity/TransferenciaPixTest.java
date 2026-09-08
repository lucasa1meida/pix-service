package com.pixservice.pix.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.StatusTransferenciaPix;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtensionMethod({ExtensoesDeTeste.class})
class TransferenciaPixTest {

    private static final String REFERENCIA = "E12345678202501151030123456789";
    private static final UUID CARTEIRA_ID_ORIGEM = UUID.randomUUID();
    private static final UUID CARTEIRA_ID_DESTINO = UUID.randomUUID();
    private static final String CHAVE_PIX = "usuario@example.com";
    private static final BigDecimal VALOR = new BigDecimal("100.00");

    @Test
    @DisplayName("Deve instanciar TransferenciaPix com dados válidos e status PENDENTE")
    void deveInstanciarTransferenciaPix() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);

        assertEquals(REFERENCIA, transferencia.getReferencia());
        assertEquals(CARTEIRA_ID_ORIGEM, transferencia.getCarteiraIdOrigem());
        assertEquals(CARTEIRA_ID_DESTINO, transferencia.getCarteiraIdDestino());
        assertEquals(CHAVE_PIX, transferencia.getChavePix());
        assertEquals(VALOR, transferencia.getValor());
        assertEquals(StatusTransferenciaPix.PENDENTE, transferencia.getStatus());
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando referência for nula ou vazia")
    void deveRetornarExcecaoCasoReferenciaNulaOuVazia(String referencia) {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(referencia, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR))
                .comMensagemDeErro("Referência não pode ser nula.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteiraIdOrigem for nula")
    void deveRetornarExcecaoCasoCarteiraIdOrigemNula() {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, null,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR))
                .comMensagemDeErro("Carteira Id Origem não pode ser nula.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteiraIdDestino for nula")
    void deveRetornarExcecaoCasoCarteiraIdDestinoNula() {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                null, CHAVE_PIX, VALOR))
                .comMensagemDeErro("Carteira Id Destino não pode ser nula.");
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando chavePix for nula ou vazia")
    void deveRetornarExcecaoCasoChavePixNulaOuVazia(String chavePix) {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, chavePix, VALOR))
                .comMensagemDeErro("Chave Pix não pode ser nula.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for nulo")
    void deveRetornarExcecaoCasoValorNulo() {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, null))
                .comMensagemDeErro("Valor da transferência deve ser positivo.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for zero")
    void deveRetornarExcecaoCasoValorZero() {
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, BigDecimal.ZERO))
                .comMensagemDeErro("Valor da transferência deve ser positivo.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for negativo")
    void deveRetornarExcecaoCasoValorNegativo() {
        BigDecimal valor = new BigDecimal("-10.00");
        assertThrows(ExcecaoDeDominio.class, () -> new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, valor))
                .comMensagemDeErro("Valor da transferência deve ser positivo.");
    }

    @Test
    @DisplayName("Deve confirmar transferência pendente")
    void deveConfirmarTransferenciaPendente() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);

        TransferenciaPix transferenciaConfirmada = transferencia.confirmar();

        assertEquals(StatusTransferenciaPix.CONFIRMADO, transferenciaConfirmada.getStatus());
        assertEquals(REFERENCIA, transferenciaConfirmada.getReferencia());
        assertEquals(VALOR, transferenciaConfirmada.getValor());
    }

    @Test
    @DisplayName("Deve rejeitar transferência pendente")
    void deveRejeitarTransferenciaPendente() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);

        TransferenciaPix transferenciaRejeitada = transferencia.rejeitar();

        assertEquals(StatusTransferenciaPix.REJEITADO, transferenciaRejeitada.getStatus());
        assertEquals(REFERENCIA, transferenciaRejeitada.getReferencia());
        assertEquals(VALOR, transferenciaRejeitada.getValor());
    }

    @Test
    @DisplayName("Deve retornar a mesma transferência ao confirmar já confirmada")
    void deveRetornarMesmaTransferenciaAoConfirmarJaConfirmada() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);
        TransferenciaPix transferenciaConfirmada = transferencia.confirmar();

        TransferenciaPix resultado = transferenciaConfirmada.confirmar();

        assertEquals(StatusTransferenciaPix.CONFIRMADO, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve retornar a mesma transferência ao rejeitar já rejeitada")
    void deveRetornarMesmaTransferenciaAoRejeitarJaRejeitada() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);
        TransferenciaPix transferenciaRejeitada = transferencia.rejeitar();

        TransferenciaPix resultado = transferenciaRejeitada.rejeitar();

        assertEquals(StatusTransferenciaPix.REJEITADO, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao confirmar transferência rejeitada")
    void deveRetornarExcecaoAoConfirmarTransferenciaRejeitada() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);
        TransferenciaPix transferenciaRejeitada = transferencia.rejeitar();

        assertThrows(ExcecaoDeDominio.class, transferenciaRejeitada::confirmar)
                .comMensagemDeErro("Não é possível confirmar a transferência de Pix que tenha estado REJEITADO");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao rejeitar transferência confirmada")
    void deveRetornarExcecaoAoRejeitarTransferenciaConfirmada() {
        TransferenciaPix transferencia = new TransferenciaPix(REFERENCIA, CARTEIRA_ID_ORIGEM,
                CARTEIRA_ID_DESTINO, CHAVE_PIX, VALOR);
        TransferenciaPix transferenciaConfirmada = transferencia.confirmar();

        assertThrows(ExcecaoDeDominio.class, transferenciaConfirmada::rejeitar)
                .comMensagemDeErro("Não é possível confirmar a transferência de Pix do estado CONFIRMADO");
    }
}

