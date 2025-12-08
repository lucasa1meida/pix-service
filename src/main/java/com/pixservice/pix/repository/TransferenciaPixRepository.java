package com.pixservice.pix.repository;

import com.pixservice.pix.entity.TransferenciaPix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransferenciaPixRepository extends JpaRepository<TransferenciaPix, UUID> {
    Optional<TransferenciaPix> findByReferencia(String referencia);
}
