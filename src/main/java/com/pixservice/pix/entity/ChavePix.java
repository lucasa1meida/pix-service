package com.pixservice.pix.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.TipoChavePix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(schema = "public", name = "chave_pix", uniqueConstraints = {
        @UniqueConstraint(name = "uk_valor_chave", columnNames = "valor_chave")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChavePix {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "carteira_id", nullable = false)
    private UUID carteiraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave_pix", nullable = false)
    private TipoChavePix tipoChavePix;

    @Column(name = "valor_chave", nullable = false)
    private String valorChave;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "data_de_criacao", nullable = false, updatable = false)
    private LocalDateTime dataDeCriacao;

    @Column(name = "data_de_atualizacao", nullable = false)
    private LocalDateTime dataDeAtualizacao;

    public ChavePix(UUID carteiraId, TipoChavePix tipoChavePix, String valorChave) {
        validarCamposObrigatorios(carteiraId, tipoChavePix, valorChave);
        this.id = UUID.randomUUID();
        this.carteiraId = carteiraId;
        this.tipoChavePix = tipoChavePix;
        this.valorChave = valorChave;
        this.ativo = true;

        var dataDeCriacaoAtualizacao = LocalDateTime.now();
        this.dataDeCriacao = dataDeCriacaoAtualizacao;
        this.dataDeAtualizacao = dataDeCriacaoAtualizacao;
    }

    private static void validarCamposObrigatorios(UUID carteiraId, TipoChavePix tipoChavePix, String valorChave){
        ExcecaoDeDominio.quandoObjetoForNulo(carteiraId,"carteiraId não pode ser nulo");
        ExcecaoDeDominio.quandoObjetoForNulo(tipoChavePix,"tipoChavePix não pode ser nulo");
        ExcecaoDeDominio.quandoStringForVazia(valorChave, "valorChave não pode ser nulo");
    }
}