package com.pixservice.pix.repository;

import com.pixservice.pix.entity.ChavePix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChavePixRepository extends JpaRepository<ChavePix, UUID> {
    Optional<ChavePix> findByValorChaveAndAtivoTrue(String chavePixDestino);
}