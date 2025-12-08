package com.pixservice.pix.service;

import com.pixservice.pix.dto.ChavePixDTO;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.enums.TipoChavePix;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPixService {
    ChavePixDTO registrarChavePix(UUID carteiraId, TipoChavePix type, String value);

    TransferenciaPixDTO transferir(String chaveIndepotencia, UUID carteiraId, String chavePixDestino, BigDecimal valorTransferencia);

    TransferenciaPixDTO processarWebhook(RequisicaoPixWebhookDTO request);
}