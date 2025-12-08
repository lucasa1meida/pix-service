package com.pixservice.pix.service;

import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.ChavePix;
import com.pixservice.pix.entity.RegistroIdempotencia;
import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.repository.ChavePixRepository;
import com.pixservice.pix.repository.RegistroIdempotenciaRepository;
import com.pixservice.pix.repository.TransferenciaPixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferenciaPixService implements ITransferenciaPixService {

    private final CarteiraRepository carteiraRepository;
    private final ChavePixRepository chavePixRepository;
    private final RegistroIdempotenciaRepository registroIdempotenciaRepository;
    private final TransferenciaPixRepository transferenciaPixRepository;
    private final ModelMapper modelMapper;

    @Override
    public TransferenciaPixDTO obterTransferenciaPixExistente(String chaveIdempotencia, String payloadHash) {
        RegistroIdempotencia registroIdempotencia = registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia);
        if (registroIdempotencia != null) {
            if (!Objects.equals(payloadHash, registroIdempotencia.getPayloadHash())) {
                throw new ExcecaoDeDominio("A chave de idempotência já está sendo usado por outro paylod.");
            }

            String referencia = registroIdempotencia.getReferencia();
            if (referencia != null && !referencia.isBlank()) {
                TransferenciaPix transferenciaPix = transferenciaPixRepository.findByReferencia(referencia)
                        .orElseThrow(() -> new ExcecaoDeDominio("Transferência Pix não encontrada para a referência " + referencia));
                return modelMapper.map(transferenciaPix, TransferenciaPixDTO.class);
            }
        }
        return null;
    }

    @Override
    public TransferenciaPix obterTransferenciaPixNova(String chaveIdempotencia, UUID carteiraIdOrigem, String chavePixDestino,
                                                      BigDecimal valorTransferencia, String payloadHash) {
        Carteira carteiraOrigem = carteiraRepository.findWithLockingById(carteiraIdOrigem)
                .orElseThrow(() -> new ExcecaoDeDominio("Carteira de origem não encontrada: " + carteiraIdOrigem));

        ChavePix chavePixDestinatario = chavePixRepository.findByValorChaveAndAtivoTrue(chavePixDestino)
                .orElseThrow(() -> new ExcecaoDeDominio("Chave pix de destino não foi encontrada ou está inativa."));

        Carteira carteiraDestino = carteiraRepository.findWithLockingById(chavePixDestinatario.getCarteiraId())
                .orElseThrow(() -> new ExcecaoDeDominio("Carteira de destino não encontrada: " + chavePixDestinatario.getCarteiraId()));

        String endToEndId = UUID.randomUUID().toString();
        TransferenciaPix transferenciaPix = new TransferenciaPix(endToEndId, carteiraDestino.getId(), carteiraOrigem.getId(),
                chavePixDestinatario.getValorChave(), valorTransferencia);
        transferenciaPixRepository.save(transferenciaPix);

        RegistroIdempotencia registroIdempotencia = new RegistroIdempotencia(chaveIdempotencia, endToEndId, payloadHash);
        registroIdempotenciaRepository.saveAndFlush(registroIdempotencia);

        log.info("Transferência Pix iniciada para referência={}, carteiraIdOrigem={}, carteiraDestinoId={}, valorTransferencia={}, chaveIdempotencia={}",
                endToEndId, carteiraIdOrigem, carteiraDestino.getId(), valorTransferencia, chaveIdempotencia);

        return transferenciaPix;
    }
}