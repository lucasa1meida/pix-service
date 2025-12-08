package com.pixservice.pix.entity;

import com.ExtensoesDeTeste;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.TipoChavePix;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtensionMethod({ExtensoesDeTeste.class})
class ChavePixTest {

    @Test
    @DisplayName("Deve instanciar ChavePix com dados válidos")
    void deveInstanciarChavePix() {
        UUID carteiraId = UUID.randomUUID();
        ChavePix chavePix = new ChavePix(carteiraId, TipoChavePix.EMAIL, "valor-da-chave");

        assertEquals(chavePix.getCarteiraId(), carteiraId);
        assertEquals("valor-da-chave", chavePix.getValorChave());
        assertEquals(TipoChavePix.EMAIL, chavePix.getTipoChavePix());
        assertTrue(chavePix.isAtivo());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteiraId for nulo na ChavePix")
    void deveRetornarExcecaoCasoCarteiraIdNulo() {
        assertThrows(ExcecaoDeDominio.class, () -> new ChavePix(null, TipoChavePix.EMAIL, "valor-da-chave"))
                .comMensagemDeErro("carteiraId não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando tipoChavePix for nulo")
    void deveRetornarExcecaoCasoTipoChavePixNulo() {
        UUID carteiraId = UUID.randomUUID();
        assertThrows(ExcecaoDeDominio.class, () -> new ChavePix(carteiraId, null, "valor-da-chave"))
                .comMensagemDeErro("tipoChavePix não pode ser nulo");
    }

    @NullAndEmptySource
    @ParameterizedTest
    @DisplayName("Deve lançar ExcecaoDeDominio quando valorChave for nulo ou vazio")
    void deveRetornarExcecaoCasoValorChavePixNulo(String valorChave) {
        UUID carteiraId = UUID.randomUUID();
        assertThrows(ExcecaoDeDominio.class, () -> new ChavePix(carteiraId, TipoChavePix.EMAIL, valorChave))
                .comMensagemDeErro("valorChave não pode ser nulo");
    }
}