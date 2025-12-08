package com.pixservice.comum.excecoes;

import java.util.Objects;

public class ExcecaoDeDominio extends RuntimeException {

    public ExcecaoDeDominio(String mensagemDeErro) {
        super(mensagemDeErro);
    }

    public static void quandoObjetoForNulo(Object objeto, String mensagemDeErro) {
        if (Objects.isNull(objeto))
            entaoDisparar(mensagemDeErro);
    }

    public static void quandoStringForVazia(String valor, String mensagemDeErro) {
        if (Objects.isNull(valor) || valor.isBlank())
            entaoDisparar(mensagemDeErro);
    }

    private static void entaoDisparar(String mensagemDeErro) {
        throw new ExcecaoDeDominio(mensagemDeErro);
    }
}