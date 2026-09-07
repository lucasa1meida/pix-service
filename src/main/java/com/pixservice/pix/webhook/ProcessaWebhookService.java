package com.pixservice.pix.webhook;

import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import com.pixservice.historico_transacao.enums.TipoTransacao;
import com.pixservice.historico_transacao.repository.HistoricoTransacaoRepository;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.EventoPixWebhook;
import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
import com.pixservice.pix.repository.EventoWebhookRepository;
import com.pixservice.pix.repository.TransferenciaPixRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessaWebhookService implements  IProcessaWebhookService {

    private final EventoWebhookRepository eventoWebhookRepository;
    private final TransferenciaPixRepository transferenciaPixRepository;
    private final ModelMapper modelMapper;
    private final HistoricoTransacaoRepository historicoTransacaoRepository;
    private final CarteiraRepository carteiraRepository;

    @Transactional
    @Override
    public TransferenciaPixDTO processar(@Valid RequisicaoPixWebhookDTO request) {
        String referencia = request.getReferencia();
        Optional<EventoPixWebhook> eventoExistente = eventoWebhookRepository.findByEventoId(request.getEventoId());
        if (eventoExistente.isPresent()) {
            log.info("Evento de Webhook {} já processado, ignorando o reprocessamento.", request.getEventoId());
            TransferenciaPix transferenciaPix = transferenciaPixRepository.findByReferencia(referencia)
                    .orElseThrow(() -> new ExcecaoDeDominio("Transferência Pix não encontrada para a referência " + referencia));
            return modelMapper.map(transferenciaPix, TransferenciaPixDTO.class);
        }

        EventoPixWebhook eventoNovo = new EventoPixWebhook(request.getEventoId(), referencia,
                request.getTipoEvento(), request.getDataDeOcorrencia(), LocalDateTime.now());
        eventoWebhookRepository.save(eventoNovo);

        TransferenciaPix transferenciaPix = transferenciaPixRepository.findByReferencia(referencia)
                .orElseThrow(() -> new ExcecaoDeDominio("Transferência Pix não encontrada para a referência " + referencia));

        TransferenciaPix transferenciaPixAtualizada;

        try {
            if (request.getTipoEvento() == TipoEventoPixWebhook.CONFIRMADO) {
                transferenciaPixAtualizada = transferenciaPix.confirmar();

                HistoricoTransacao debito = HistoricoTransacao.depositar(transferenciaPixAtualizada.getCarteiraIdOrigem(),
                        transferenciaPixAtualizada.getValor(), TipoTransacao.PIX_OUT, transferenciaPixAtualizada.getReferencia());
                HistoricoTransacao credito = HistoricoTransacao.depositar(transferenciaPixAtualizada.getCarteiraIdDestino(),
                        transferenciaPixAtualizada.getValor(), TipoTransacao.PIX_IN, transferenciaPixAtualizada.getReferencia());

                historicoTransacaoRepository.save(debito);
                historicoTransacaoRepository.save(credito);

                Carteira carteiraOrigem = carteiraRepository.findWithLockingById(transferenciaPixAtualizada.getCarteiraIdOrigem())
                        .orElseThrow(() -> new ExcecaoDeDominio("Carteira de origem não encontrada: " + transferenciaPixAtualizada.getCarteiraIdOrigem()));
                Carteira carteiraDestino = carteiraRepository.findWithLockingById(transferenciaPixAtualizada.getCarteiraIdDestino())
                        .orElseThrow(() -> new ExcecaoDeDominio("Carteira de destino não encontrada: " + transferenciaPixAtualizada.getCarteiraIdDestino()));

                carteiraOrigem.sacar(transferenciaPixAtualizada.getValor());
                carteiraDestino.depositar(transferenciaPixAtualizada.getValor());
            } else {
                transferenciaPixAtualizada = transferenciaPix.rejeitar();
            }
        } catch (ExcecaoDeDominio e) {
            return modelMapper.map(transferenciaPix, TransferenciaPixDTO.class);
        }

        transferenciaPixRepository.save(transferenciaPixAtualizada);
        return modelMapper.map(transferenciaPixAtualizada, TransferenciaPixDTO.class);
    }
}
