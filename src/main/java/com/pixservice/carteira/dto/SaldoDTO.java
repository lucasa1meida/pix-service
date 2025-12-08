package com.pixservice.carteira.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SaldoDTO {
    private BigDecimal saldo;
    private LocalDateTime data;
}