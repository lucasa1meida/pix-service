package com.pixservice.pix.service;

import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import com.pixservice.historico_transacao.enums.TipoTransacao;
import com.pixservice.historico_transacao.repository.HistoricoTransacaoRepository;
import com.pixservice.pix.dto.ChavePixDTO;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.ChavePix;
import com.pixservice.pix.entity.EventoPixWebhook;
import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.enums.TipoChavePix;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
import com.pixservice.pix.repository.ChavePixRepository;
import com.pixservice.pix.repository.EventoWebhookRepository;
import com.pixservice.pix.repository.TransferenciaPixRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixService implements IPixService {

    private final CarteiraRepository carteiraRepository;
    private final ChavePixRepository chavePixRepository;
    private final TransferenciaPixService transferenciaPixService;
    private final EventoWebhookRepository eventoWebhookRepository;
    private final TransferenciaPixRepository transferenciaPixRepository;
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;
    private final HistoricoTransacaoRepository historicoTransacaoRepository;

    @Transactional
    @Override
    public ChavePixDTO registrarChavePix(UUID carteiraId, TipoChavePix tipoChavePix, String valorChave) {
        carteiraRepository.findById(carteiraId)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada."));

        ChavePix chavePix = new ChavePix(carteiraId, tipoChavePix, valorChave);
        chavePixRepository.save(chavePix);

        return modelMapper.map(chavePix, ChavePixDTO.class);
    }

    @Transactional
    @Override
    public TransferenciaPixDTO transferir(String chaveIdempotencia, UUID carteiraIdOrigem, String chavePixDestino,
                                          BigDecimal valorTransferencia) {
        String payloadHash = obterPayloadHashPor(carteiraIdOrigem, chavePixDestino, valorTransferencia);

        TransferenciaPixDTO transferenciaPixExistente = transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash);
        if (transferenciaPixExistente != null) return transferenciaPixExistente;

        TransferenciaPix transferenciaPixNova = transferenciaPixService.obterTransferenciaPixNova(chaveIdempotencia,
                carteiraIdOrigem, chavePixDestino, valorTransferencia, payloadHash);

        meterRegistry.counter("transferencias_pix_iniciadas").increment();

        return modelMapper.map(transferenciaPixNova, TransferenciaPixDTO.class);
    }

    @Transactional
    @Override
    public TransferenciaPixDTO processarWebhook(@Valid RequisicaoPixWebhookDTO request) {
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

                Carteira carteiraOrigemAtualizada = carteiraOrigem.sacar(transferenciaPixAtualizada.getValor());
                Carteira carteiraDestinoAtualizada = carteiraDestino.depositar(transferenciaPixAtualizada.getValor());
                carteiraRepository.save(carteiraOrigemAtualizada);
                carteiraRepository.save(carteiraDestinoAtualizada);

            } else {
                transferenciaPixAtualizada = transferenciaPix.rejeitar();
            }
        } catch (ExcecaoDeDominio e) {
            return modelMapper.map(transferenciaPix, TransferenciaPixDTO.class);
        }

        transferenciaPixRepository.save(transferenciaPixAtualizada);
        return modelMapper.map(transferenciaPixAtualizada, TransferenciaPixDTO.class);
    }

    private static String obterPayloadHashPor(UUID carteiraIdOrigem, String chavePixDestino, BigDecimal valorTransferencia) {
        String valorCanonico = valorTransferencia.setScale(2, RoundingMode.HALF_UP).toString();
        String payload = carteiraIdOrigem + chavePixDestino + valorCanonico;
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }
}