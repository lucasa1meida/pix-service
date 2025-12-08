package com.pixservice.historico_transacao.repository;

import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface HistoricoTransacaoRepository extends JpaRepository<HistoricoTransacao, UUID> {
    HistoricoTransacao findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(LocalDateTime dataTransacao, UUID carteiraId);
}