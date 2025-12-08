package com.pixservice.pix.repository;

import com.pixservice.pix.entity.EventoPixWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventoWebhookRepository extends JpaRepository<EventoPixWebhook, UUID> {
    Optional<EventoPixWebhook> findByEventoId(String eventoId);
}