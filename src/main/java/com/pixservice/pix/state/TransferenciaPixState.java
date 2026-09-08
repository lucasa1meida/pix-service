package com.pixservice.pix.state;

import com.pixservice.pix.entity.TransferenciaPix;

public interface TransferenciaPixState {
    TransferenciaPix confirmar(TransferenciaPix transferenciaPix);
    TransferenciaPix rejeitar(TransferenciaPix transferenciaPix);
}