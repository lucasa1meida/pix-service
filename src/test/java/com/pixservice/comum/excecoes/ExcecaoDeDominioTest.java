package com.pixservice.comum.excecoes;

import com.ExtensoesDeTeste;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtensionMethod({ExtensoesDeTeste.class})
class ExcecaoTest {

    private String mensagemEsperada;

    @BeforeEach
    @DisplayName("Deve inicializar a mensagem esperada antes de cada teste")
    void init() {
        mensagemEsperada = "Mensagem da exceção";
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "     "})
    @DisplayName("Deve lançar ExcecaoDeDominio quando string for vazia ou nula")
    void deveLancarUmaExcecaoQuandoStringForVazia(String texto) {
        assertThrows(ExcecaoDeDominio.class, () -> ExcecaoDeDominio.quandoStringForVazia(texto, mensagemEsperada)).comMensagemDeErro(mensagemEsperada);
        assertThrows(ExcecaoDeDominio.class, () -> ExcecaoDeDominio.quandoStringForVazia(null, mensagemEsperada)).comMensagemDeErro(mensagemEsperada);
        assertDoesNotThrow(() -> ExcecaoDeDominio.quandoStringForVazia("Tem texto", mensagemEsperada));
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando objeto for nulo")
    void deveLancarExcecaoSeObjetoVierNulo() {
        assertThrows(ExcecaoDeDominio.class, () -> ExcecaoDeDominio.quandoObjetoForNulo(null, mensagemEsperada)).comMensagemDeErro(mensagemEsperada);
        assertDoesNotThrow(() -> ExcecaoDeDominio.quandoObjetoForNulo(new Object(), mensagemEsperada));
    }
}