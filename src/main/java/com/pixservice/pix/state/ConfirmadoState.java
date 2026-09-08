package com.pixservice.pix.state;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.entity.TransferenciaPix;

public class ConfirmadoState implements TransferenciaPixState {

    @Override
    public TransferenciaPix confirmar(TransferenciaPix transferenciaPix) {
        return transferenciaPix;
    }

    @Override
    public TransferenciaPix rejeitar(TransferenciaPix transferenciaPix) {
        throw new ExcecaoDeDominio("Não é possível confirmar a transferência de Pix do estado CONFIRMADO");
    }
}
