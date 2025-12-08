package com.pixservice.carteira.service;

import com.pixservice.carteira.dto.CarteiraDTO;
import com.pixservice.carteira.dto.SaldoDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ICarteiraService {

    CarteiraDTO criar(String carteiraDTO);

    SaldoDTO obterSaldo(UUID carteiraID, LocalDateTime dataDeCorte);

    CarteiraDTO depositar(UUID carteiraId, BigDecimal quantidade);

    CarteiraDTO sacar(UUID carteiraId, BigDecimal quantidade);
}