package com.pixservice.carteira.repository;

import com.pixservice.carteira.entity.Carteira;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface CarteiraRepository extends JpaRepository<Carteira, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carteira> findWithLockingById(UUID carteiraIdOrigem);
}