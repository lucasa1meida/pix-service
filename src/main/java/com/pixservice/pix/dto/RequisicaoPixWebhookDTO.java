package com.pixservice.pix.dto;

import com.pixservice.pix.enums.TipoEventoPixWebhook;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequisicaoPixWebhookDTO {
    private String referencia;
    private String eventoId;
    private TipoEventoPixWebhook tipoEvento;
    private LocalDateTime dataDeOcorrencia;
}