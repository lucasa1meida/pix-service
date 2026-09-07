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
import com.pixservice.pix.webhook.IProcessaWebhookService;
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
    private final ModelMapper modelMapper;
    private final MeterRegistry meterRegistry;
    private final IProcessaWebhookService processaWebhookService;

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
        return processaWebhookService.processar(request);
    }

    private static String obterPayloadHashPor(UUID carteiraIdOrigem, String chavePixDestino, BigDecimal valorTransferencia) {
        String valorCanonico = valorTransferencia.setScale(2, RoundingMode.HALF_UP).toString();
        String payload = carteiraIdOrigem + chavePixDestino + valorCanonico;
        return DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
    }
}