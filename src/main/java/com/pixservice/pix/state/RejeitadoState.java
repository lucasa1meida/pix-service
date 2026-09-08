package com.pixservice.pix.state;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.entity.TransferenciaPix;

public class RejeitadoState implements TransferenciaPixState {

    @Override
    public TransferenciaPix confirmar(TransferenciaPix transferenciaPix) {
        throw new ExcecaoDeDominio("Não é possível confirmar a transferência de Pix que tenha estado REJEITADO");
    }

    @Override
    public TransferenciaPix rejeitar(TransferenciaPix transferenciaPix) {
        return transferenciaPix;
    }
}
