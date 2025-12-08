package com.pixservice.pix.repository;

import com.pixservice.pix.entity.RegistroIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroIdempotenciaRepository extends JpaRepository<RegistroIdempotencia, UUID> {
    RegistroIdempotencia findByChaveIdempotencia(String chaveIdempotencia);
}