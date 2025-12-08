package com.pixservice.pix.service;

import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.TransferenciaPix;

import java.math.BigDecimal;
import java.util.UUID;

public interface ITransferenciaPixService {
    TransferenciaPixDTO obterTransferenciaPixExistente(String chaveIdempotencia, String payloadHash);

    TransferenciaPix obterTransferenciaPixNova(String chaveIdempotencia, UUID carteiraIdOrigem, String chavePixDestino,
                                               BigDecimal valorTransferencia, String payloadHash);
}