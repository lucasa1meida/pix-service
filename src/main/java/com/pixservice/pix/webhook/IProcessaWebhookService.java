package com.pixservice.pix.webhook;

import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;

public interface IProcessaWebhookService {
    TransferenciaPixDTO processar(RequisicaoPixWebhookDTO request);
}
