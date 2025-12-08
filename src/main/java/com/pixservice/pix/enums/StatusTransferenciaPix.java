package com.pixservice.pix.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusTransferenciaPix {
    PENDENTE("PENDENTE"),
    CONFIRMADO("CONFIRMADO"),
    REJEITADO("REJEITADO");

    private final String valor;
}