package com.pixservice.pix.state;

import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.enums.StatusTransferenciaPix;

public class PendenteState implements TransferenciaPixState {

    @Override
    public TransferenciaPix confirmar(TransferenciaPix transferenciaPix) {
        return transferenciaPix.comStatus(StatusTransferenciaPix.CONFIRMADO);
    }

    @Override
    public TransferenciaPix rejeitar(TransferenciaPix transferenciaPix) {
        return transferenciaPix.comStatus(StatusTransferenciaPix.REJEITADO);
    }
}
