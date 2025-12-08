package com.pixservice.pix.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(schema = "public", name = "registro_idempotencia", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chave_idempotencia", columnNames = {"chave_idempotencia"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistroIdempotencia {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "chave_idempotencia", nullable = false)
    private String chaveIdempotencia;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "data_de_criacao", nullable = false)
    private LocalDateTime dataDeCriacao;

    @Column(name = "payload_hash", nullable = false)
    private String payloadHash;

    public RegistroIdempotencia(String chaveIdempotencia, String referencia, String payloadHash) {
        validarCamposObrigatorios(chaveIdempotencia, referencia, payloadHash);
        this.id = UUID.randomUUID();
        this.chaveIdempotencia = chaveIdempotencia;
        this.referencia = referencia;
        this.dataDeCriacao = LocalDateTime.now();
        this.payloadHash = payloadHash;
    }

    private static void validarCamposObrigatorios(String chaveIdempotencia, String referencia, String payloadHash) {
        ExcecaoDeDominio.quandoStringForVazia(chaveIdempotencia, "chaveIdempotencia não pode ser nula ou vazia");
        ExcecaoDeDominio.quandoStringForVazia(referencia, "referencia não pode ser nula ou vazia");
        ExcecaoDeDominio.quandoStringForVazia(payloadHash, "payloadHash não pode ser nulo ou vazio");
    }
}