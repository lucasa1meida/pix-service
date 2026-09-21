package com.pixservice.carteira.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtensionMethod({ExtensoesDeTeste.class})
class CarteiraTest {

    @Test
    @DisplayName("Deve instanciar carteira com saldo zero e customer informado")
    void deveInstanciarCarteira() {
        String customerId = "user-123";
        Carteira carteira = new Carteira(customerId);

        assertEquals(customerId, carteira.getCustomerId());
        assertEquals(BigDecimal.ZERO, carteira.getSaldo());
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando customerId for nulo ou vazio")
    void deveRetornarExcecaoCasocustomerIdNuloOuVazio(String customerId) {
        assertThrows(ExcecaoDeDominio.class, () -> new Carteira(customerId))
                .comMensagemDeErro("customerId não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve depositar valor atualizando o saldo")
    void deveDepositarValor() {
        Carteira carteira = new Carteira("user-123");
        BigDecimal valorDeposito = new BigDecimal("100.00");

        Carteira carteiraAtualizada = carteira.depositar(valorDeposito);

        assertEquals(new BigDecimal("100.00"), carteiraAtualizada.getSaldo());
    }

    @Test
    @DisplayName("Deve sacar valor atualizando o saldo")
    void deveSacarValor() {
        Carteira carteira = new Carteira("user-123");
        carteira.depositar(new BigDecimal("100.00"));
        BigDecimal valorSaque = new BigDecimal("50.00");

        Carteira carteiraAtualizada = carteira.sacar(valorSaque);

        assertEquals(new BigDecimal("50.00"), carteiraAtualizada.getSaldo());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao depositar com valor nulo")
    void deveRetornarExcecaoCasoDepositoComValorNulo() {
        Carteira carteira = new Carteira("user-123");
        assertThrows(ExcecaoDeDominio.class, () -> carteira.depositar(null))
                .comMensagemDeErro("Quantidade a depositar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao depositar com valor zero")
    void deveRetornarExcecaoCasoDepositoComValorZero() {
        Carteira carteira = new Carteira("user-123");
        assertThrows(ExcecaoDeDominio.class, () -> carteira.depositar(BigDecimal.ZERO))
                .comMensagemDeErro("Quantidade a depositar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao depositar com valor negativo")
    void deveRetornarExcecaoCasoDepositoComValorNegativo() {
        Carteira carteira = new Carteira("user-123");
        BigDecimal valor = new BigDecimal("-10.00");

        assertThrows(ExcecaoDeDominio.class, () -> carteira.depositar(valor))
                .comMensagemDeErro("Quantidade a depositar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao sacar com valor nulo")
    void deveRetornarExcecaoCasoSaqueComValorNulo() {
        Carteira carteira = new Carteira("user-123");
        assertThrows(ExcecaoDeDominio.class, () -> carteira.sacar(null))
                .comMensagemDeErro("Quantidade a retirar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao sacar com valor zero")
    void deveRetornarExcecaoCasoSaqueComValorZero() {
        Carteira carteira = new Carteira("user-123");
        assertThrows(ExcecaoDeDominio.class, () -> carteira.sacar(BigDecimal.ZERO))
                .comMensagemDeErro("Quantidade a retirar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao sacar com valor negativo")
    void deveRetornarExcecaoCasoSaqueComValorNegativo() {
        Carteira carteira = new Carteira("user-123");
        BigDecimal valor = new BigDecimal("-10.00");

        assertThrows(ExcecaoDeDominio.class, () -> carteira.sacar(valor))
                .comMensagemDeErro("Quantidade a retirar deve ser positiva.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio ao sacar com fundos insuficientes")
    void deveRetornarExcecaoCasoSaqueComSaldoInsuficiente() {
        Carteira carteira = new Carteira("user-123");
        carteira.depositar(new BigDecimal("50.00"));
        BigDecimal valor = new BigDecimal("100.00");

        assertThrows(ExcecaoDeDominio.class, () -> carteira.sacar(valor))
                .comMensagemDeErro("Fundos insuficientes.");
    }
}

