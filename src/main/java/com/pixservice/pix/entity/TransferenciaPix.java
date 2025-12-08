package com.pixservice.pix.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.StatusTransferenciaPix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "public", name = "transferencia_pix")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransferenciaPix {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "carteira_id_origem")
    private UUID carteiraIdOrigem;

    @Column(name = "carteira_id_destino")
    private UUID carteiraIdDestino;

    @Column(name = "chave_pix")
    private String chavePix;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "data_de_criacao")
    private LocalDateTime dataDeCriacao;

    @Column(name = "data_de_atualizacao")
    private LocalDateTime dataDeAtualizacao;

    @Column(name = "status")
    private StatusTransferenciaPix status;

    public TransferenciaPix(String referencia,
                            UUID carteiraIdOrigem,
                            UUID carteiraIdDestino,
                            String chavePix,
                            BigDecimal valor) {
        validarCamposObrigatorios(referencia, carteiraIdOrigem, carteiraIdDestino, chavePix, valor);
        this.id = UUID.randomUUID();
        this.referencia = referencia;
        this.carteiraIdOrigem = carteiraIdOrigem;
        this.carteiraIdDestino = carteiraIdDestino;
        this.chavePix = chavePix;
        this.valor = valor;
        this.dataDeCriacao = LocalDateTime.now();
        this.dataDeAtualizacao = LocalDateTime.now();
        this.status = StatusTransferenciaPix.PENDENTE;
    }

    private TransferenciaPix(UUID id,
                             String referencia,
                             UUID carteiraIdOrigem,
                             UUID carteiraIdDestino,
                             String chavePix,
                             BigDecimal valor,
                             LocalDateTime dataDeCriacao,
                             StatusTransferenciaPix status) {
        validarCamposObrigatorios(referencia, carteiraIdOrigem, carteiraIdDestino, chavePix, valor);
        this.id = id;
        this.referencia = referencia;
        this.carteiraIdOrigem = carteiraIdOrigem;
        this.carteiraIdDestino = carteiraIdDestino;
        this.chavePix = chavePix;
        this.valor = valor;
        this.dataDeCriacao = dataDeCriacao;
        this.dataDeAtualizacao = LocalDateTime.now();
        this.status = status;
    }

    private static void validarCamposObrigatorios(String referencia,
                                                    UUID carteiraIdOrigem,
                                                    UUID carteiraIdDestino,
                                                    String chavePix,
                                                    BigDecimal valor) {
        ExcecaoDeDominio.quandoStringForVazia(referencia, "Referência não pode ser nula.");
        ExcecaoDeDominio.quandoObjetoForNulo(carteiraIdOrigem, "Carteira Id Origem não pode ser nula.");
        ExcecaoDeDominio.quandoObjetoForNulo(carteiraIdDestino, "Carteira Id Destino não pode ser nula.");
        ExcecaoDeDominio.quandoStringForVazia(chavePix, "Chave Pix não pode ser nula.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcecaoDeDominio("Valor da transferência deve ser positivo.");
        }
    }

    public TransferenciaPix confirmar() {
        if (this.status != StatusTransferenciaPix.PENDENTE) {
            if (this.status == StatusTransferenciaPix.CONFIRMADO) {
                return this;
            }
            throw new ExcecaoDeDominio("Não é possível confirmar a transferência de Pix do estado " + status);
        }
        return new TransferenciaPix(id, referencia, carteiraIdOrigem, carteiraIdDestino, chavePix, valor, dataDeCriacao,
                StatusTransferenciaPix.CONFIRMADO);
    }

    public TransferenciaPix rejeitar() {
        if (this.status != StatusTransferenciaPix.PENDENTE) {
            if (this.status == StatusTransferenciaPix.REJEITADO) {
                return this;
            }
            throw new ExcecaoDeDominio("Não é possível confirmar a transferência de Pix do estado " + status);
        }
        return new TransferenciaPix(id, referencia, carteiraIdOrigem, carteiraIdDestino, chavePix, valor, dataDeCriacao,
                StatusTransferenciaPix.REJEITADO);
    }
}