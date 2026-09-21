package com.pixservice.pix.entity;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
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
@Table(schema = "public", name = "evento_pix_webhook", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pix_webhook_evento_id", columnNames = "evento_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventoPixWebhook {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private String eventoId;

    @Column(name = "end_to_end_id", nullable = false)
    private String endToEndId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEventoPixWebhook tipo;

    @Column(name = "data_de_ocorrencia", nullable = false)
    private LocalDateTime dataDeOcorrencia;

    @Column(name = "data_de_recebimento", nullable = false)
    private LocalDateTime dataDeRecebimento;

    public EventoPixWebhook(String eventoId,
                            String endToEndId,
                            TipoEventoPixWebhook tipo,
                            LocalDateTime dataDeOcorrencia,
                            LocalDateTime dataDeRecebimento) {
        validarCamposObrigatorios(eventoId, endToEndId, tipo, dataDeOcorrencia, dataDeRecebimento);
        this.id = UUID.randomUUID();
        this.eventoId = eventoId;
        this.endToEndId = endToEndId;
        this.tipo = tipo;
        this.dataDeOcorrencia = dataDeOcorrencia;
        this.dataDeRecebimento = dataDeRecebimento;
    }

    private static void validarCamposObrigatorios(String eventoId,
                                                   String endToEndId,
                                                   TipoEventoPixWebhook tipo,
                                                   LocalDateTime dataDeOcorrencia,
                                                   LocalDateTime dataDeRecebimento) {
        ExcecaoDeDominio.quandoStringForVazia(eventoId, "eventoId não pode ser nulo ou vazio");
        ExcecaoDeDominio.quandoStringForVazia(endToEndId, "endToEndId não pode ser nulo ou vazio");
        ExcecaoDeDominio.quandoObjetoForNulo(tipo, "tipo não pode ser nulo");
        ExcecaoDeDominio.quandoObjetoForNulo(dataDeOcorrencia, "dataDeOcorrencia não pode ser nula");
        ExcecaoDeDominio.quandoObjetoForNulo(dataDeRecebimento, "dataDeRecebimento não pode ser nula");
    }
}