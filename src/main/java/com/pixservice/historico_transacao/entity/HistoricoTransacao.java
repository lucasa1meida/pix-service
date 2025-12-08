package com.pixservice.historico_transacao.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.historico_transacao.enums.NaturezaTransacao;
import com.pixservice.historico_transacao.enums.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "public", name = "historico_transacao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoricoTransacao {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "carteira_id", nullable = false)
    private UUID carteiraId;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transacao", nullable = false)
    private TipoTransacao tipoTransacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "natureza_transacao", nullable = false)
    private NaturezaTransacao naturezaTransacao;

    @Column(name = "end_to_end_id")
    private String endToEndId;

    @Column(name = "data_transacao", nullable = false)
    private LocalDateTime dataTransacao;

    private HistoricoTransacao(UUID id, UUID carteiraId, BigDecimal valor, TipoTransacao tipoTransacao,
                               NaturezaTransacao naturezaTransacao, String endToEndId, LocalDateTime dataTransacao) {
        validarCamposObrigatorios(carteiraId, valor, tipoTransacao, naturezaTransacao, dataTransacao);
        this.id = id;
        this.carteiraId = carteiraId;
        this.valor = valor;
        this.tipoTransacao = tipoTransacao;
        this.naturezaTransacao = naturezaTransacao;
        this.endToEndId = endToEndId;
        this.dataTransacao = dataTransacao;
    }

    private static void validarCamposObrigatorios(UUID carteiraId, BigDecimal valor, TipoTransacao tipoTransacao,
                                                   NaturezaTransacao naturezaTransacao, LocalDateTime dataTransacao) {
        ExcecaoDeDominio.quandoObjetoForNulo(carteiraId, "carteiraId não pode ser nulo");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcecaoDeDominio("valor não pode ser nulo ou menor ou igual a zero");
        }
        ExcecaoDeDominio.quandoObjetoForNulo(tipoTransacao, "tipoTransacao não pode ser nulo");
        ExcecaoDeDominio.quandoObjetoForNulo(naturezaTransacao, "naturezaTransacao não pode ser nula");
        ExcecaoDeDominio.quandoObjetoForNulo(dataTransacao, "dataTransacao não pode ser nula");
    }

    public static HistoricoTransacao depositar(UUID walletId, BigDecimal valor, TipoTransacao tipoTransacao, String endToEndId) {
        return new HistoricoTransacao(UUID.randomUUID(), walletId, valor, tipoTransacao, NaturezaTransacao.CREDITO, endToEndId, LocalDateTime.now());
    }

    public static HistoricoTransacao sacar(UUID walletId, BigDecimal valor, TipoTransacao tipoTransacao, String endToEndId) {
        return new HistoricoTransacao(UUID.randomUUID(), walletId, valor, tipoTransacao, NaturezaTransacao.DEBITO, endToEndId, LocalDateTime.now());
    }
}
