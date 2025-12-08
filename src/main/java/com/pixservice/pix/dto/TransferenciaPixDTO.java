package com.pixservice.pix.dto;

import com.pixservice.pix.enums.StatusTransferenciaPix;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransferenciaPixDTO {
    private UUID id;
    private String referencia;
    private UUID carteiraIdOrigem;
    private UUID carteiraIdDestino;
    private String chavePix;
    private BigDecimal valor;
    private LocalDateTime dataDeCriacao;
    private LocalDateTime dataDeAtualizacao;
    private StatusTransferenciaPix status;
}