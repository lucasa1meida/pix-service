package com.pixservice.historico_transacao.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.historico_transacao.enums.NaturezaTransacao;
import com.pixservice.historico_transacao.enums.TipoTransacao;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtensionMethod({ExtensoesDeTeste.class})
class HistoricoTransacaoTest {

    private static final UUID CARTEIRA_ID = UUID.randomUUID();
    private static final BigDecimal VALOR = new BigDecimal("100.00");
    private static final TipoTransacao TIPO_TRANSACAO = TipoTransacao.DEPOSITO;
    private static final String END_TO_END_ID = "E12345678202501151030123456789";

    @Test
    @DisplayName("Deve instanciar histórico de transação para depósito")
    void deveInstanciarHistoricoTransacaoDeposito() {
        HistoricoTransacao historico = HistoricoTransacao.depositar(CARTEIRA_ID, VALOR, TIPO_TRANSACAO, END_TO_END_ID);

        assertNotNull(historico.getId());
        assertEquals(CARTEIRA_ID, historico.getCarteiraId());
        assertEquals(VALOR, historico.getValor());
        assertEquals(TIPO_TRANSACAO, historico.getTipoTransacao());
        assertEquals(NaturezaTransacao.CREDITO, historico.getNaturezaTransacao());
        assertEquals(END_TO_END_ID, historico.getEndToEndId());
        assertNotNull(historico.getDataTransacao());
    }

    @Test
    @DisplayName("Deve instanciar histórico de transação para saque")
    void deveInstanciarHistoricoTransacaoSaque() {
        HistoricoTransacao historico = HistoricoTransacao.sacar(CARTEIRA_ID, VALOR, TipoTransacao.SAQUE, END_TO_END_ID);

        assertNotNull(historico.getId());
        assertEquals(CARTEIRA_ID, historico.getCarteiraId());
        assertEquals(VALOR, historico.getValor());
        assertEquals(TipoTransacao.SAQUE, historico.getTipoTransacao());
        assertEquals(NaturezaTransacao.DEBITO, historico.getNaturezaTransacao());
        assertEquals(END_TO_END_ID, historico.getEndToEndId());
        assertNotNull(historico.getDataTransacao());
    }

    @Test
    @DisplayName("Deve instanciar histórico de depósito com endToEndId nulo")
    void deveInstanciarHistoricoTransacaoDepositoComEndToEndIdNulo() {
        HistoricoTransacao historico = HistoricoTransacao.depositar(CARTEIRA_ID, VALOR, TIPO_TRANSACAO, null);

        assertNotNull(historico.getId());
        assertEquals(CARTEIRA_ID, historico.getCarteiraId());
        assertEquals(VALOR, historico.getValor());
        assertEquals(NaturezaTransacao.CREDITO, historico.getNaturezaTransacao());
    }

    @Test
    @DisplayName("Deve instanciar histórico de saque com endToEndId nulo")
    void deveInstanciarHistoricoTransacaoSaqueComEndToEndIdNulo() {
        HistoricoTransacao historico = HistoricoTransacao.sacar(CARTEIRA_ID, VALOR, TipoTransacao.SAQUE, null);

        assertNotNull(historico.getId());
        assertEquals(CARTEIRA_ID, historico.getCarteiraId());
        assertEquals(VALOR, historico.getValor());
        assertEquals(NaturezaTransacao.DEBITO, historico.getNaturezaTransacao());
    }

    @Test
    @DisplayName("Deve instanciar histórico de transação para PIX_OUT")
    void deveInstanciarHistoricoTransacaoPixOut() {
        HistoricoTransacao historico = HistoricoTransacao.sacar(CARTEIRA_ID, VALOR, TipoTransacao.PIX_OUT, END_TO_END_ID);

        assertEquals(TipoTransacao.PIX_OUT, historico.getTipoTransacao());
        assertEquals(NaturezaTransacao.DEBITO, historico.getNaturezaTransacao());
    }

    @Test
    @DisplayName("Deve instanciar histórico de transação para PIX_IN")
    void deveInstanciarHistoricoTransacaoPixIn() {
        HistoricoTransacao historico = HistoricoTransacao.depositar(CARTEIRA_ID, VALOR, TipoTransacao.PIX_IN, END_TO_END_ID);

        assertEquals(TipoTransacao.PIX_IN, historico.getTipoTransacao());
        assertEquals(NaturezaTransacao.CREDITO, historico.getNaturezaTransacao());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteiraId for nulo no depósito")
    void deveRetornarExcecaoCasoCarteiraIdNuloNoDeposito() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.depositar(null, VALOR, TIPO_TRANSACAO, END_TO_END_ID))
                .comMensagemDeErro("carteiraId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteiraId for nulo no saque")
    void deveRetornarExcecaoCasoCarteiraIdNuloNoSaque() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.sacar(null, VALOR, TipoTransacao.SAQUE, END_TO_END_ID))
                .comMensagemDeErro("carteiraId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for nulo no depósito")
    void deveRetornarExcecaoCasoValorNuloNoDeposito() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.depositar(CARTEIRA_ID, null, TIPO_TRANSACAO, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for nulo no saque")
    void deveRetornarExcecaoCasoValorNuloNoSaque() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.sacar(CARTEIRA_ID, null, TipoTransacao.SAQUE, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for zero no depósito")
    void deveRetornarExcecaoCasoValorZeroNoDeposito() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.depositar(CARTEIRA_ID, BigDecimal.ZERO, TIPO_TRANSACAO, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for zero no saque")
    void deveRetornarExcecaoCasoValorZeroNoSaque() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.sacar(CARTEIRA_ID, BigDecimal.ZERO, TipoTransacao.SAQUE, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for negativo no depósito")
    void deveRetornarExcecaoCasoValorNegativoNoDeposito() {
        BigDecimal valor =  new BigDecimal("-10.00");
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.depositar(CARTEIRA_ID, valor, TIPO_TRANSACAO, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando valor for negativo no saque")
    void deveRetornarExcecaoCasoValorNegativoNoSaque() {
        BigDecimal valor =  new BigDecimal("-10.00");
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.sacar(CARTEIRA_ID, valor, TipoTransacao.SAQUE, END_TO_END_ID))
                .comMensagemDeErro("valor não pode ser nulo ou menor ou igual a zero");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando tipo de transação for nulo no depósito")
    void deveRetornarExcecaoCasoTipoTransacaoNuloNoDeposito() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.depositar(CARTEIRA_ID, VALOR, null, END_TO_END_ID))
                .comMensagemDeErro("tipoTransacao não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando tipo de transação for nulo no saque")
    void deveRetornarExcecaoCasoTipoTransacaoNuloNoSaque() {
        assertThrows(ExcecaoDeDominio.class, () -> HistoricoTransacao.sacar(CARTEIRA_ID, VALOR, null, END_TO_END_ID))
                .comMensagemDeErro("tipoTransacao não pode ser nulo");
    }
}

