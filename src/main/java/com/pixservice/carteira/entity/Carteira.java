package com.pixservice.carteira.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Table(schema = "public", name = "carteira")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carteira {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ordem_id", nullable = false)
    private String proprietarioId;

    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(name = "data_de_criacao", nullable = false, updatable = false)
    private LocalDateTime dataDeCriacao;

    @Column(name = "data_de_atualizacao", nullable = false)
    private LocalDateTime dataDeAtualizacao;

    public Carteira(String proprietarioId) {
        validarCamposObrigatorios(proprietarioId);
        this.id = UUID.randomUUID();
        this.proprietarioId = proprietarioId;
        this.saldo = BigDecimal.ZERO;

        var dataDeCriacaoAtualizacao = LocalDateTime.now();
        this.dataDeCriacao = dataDeCriacaoAtualizacao;
        this.dataDeAtualizacao = dataDeCriacaoAtualizacao;
    }

    private static void validarCamposObrigatorios(String proprietarioId) {
        ExcecaoDeDominio.quandoStringForVazia(proprietarioId, "proprietarioId não pode ser nulo ou vazio");
    }

    public Carteira depositar(BigDecimal quantidade) {
        validarSeValorEhPositivo(quantidade, "Quantidade a depositar deve ser positiva.");
        this.saldo = this.saldo.add(quantidade);
        this.dataDeAtualizacao = LocalDateTime.now();
        return this;
    }

    public Carteira sacar(BigDecimal quantidade) {
        validarSeValorEhPositivo(quantidade, "Quantidade a retirar deve ser positiva.");
        if (quantidade.compareTo(this.saldo) > 0) {
            throw new ExcecaoDeDominio("Fundos insuficientes.");
        }
        this.saldo = this.saldo.subtract(quantidade);
        this.dataDeAtualizacao = LocalDateTime.now();
        return this;
    }

    private static void validarSeValorEhPositivo(BigDecimal quantidade, String mensagemDeErro) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcecaoDeDominio(mensagemDeErro);
        }
    }
}