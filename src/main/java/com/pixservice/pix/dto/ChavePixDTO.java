package com.pixservice.pix.dto;

import com.pixservice.pix.enums.TipoChavePix;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChavePixDTO {
    private UUID id;
    private UUID carteiraId;
    private TipoChavePix tipoChavePix;
    private String valorChave;
    private boolean ativo;
    private LocalDateTime dataDeCriacao;
    private LocalDateTime dataDeAtualizacao;
}