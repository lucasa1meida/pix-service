package com.pixservice.pix.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RequisicaoPixTransferenciaDTO {
    private UUID carteiraIdOrigem;
    private String chavePixDestino;
    private BigDecimal valor;
}