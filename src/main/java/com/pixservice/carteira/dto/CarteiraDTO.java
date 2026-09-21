package com.pixservice.carteira.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CarteiraDTO {
    private UUID carteiraId;
    private String customerId;
    private BigDecimal saldo;
    private LocalDateTime dataDeCriacao;
    private LocalDateTime dataDeAtualizacao;
}