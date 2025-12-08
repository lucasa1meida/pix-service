package com.pixservice.pix.controller;

import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.comum.excecoes.InternalServerErrorException;
import com.pixservice.pix.dto.ChavePixDTO;
import com.pixservice.pix.dto.RegistroChavePixDTO;
import com.pixservice.pix.dto.RequisicaoPixTransferenciaDTO;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.service.PixService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    @PostMapping("/carteiras/{carteiraId}/chaves-pix")
    @ResponseStatus(HttpStatus.CREATED)
    public ChavePixDTO registrarChavePix(@PathVariable("carteiraId") UUID walletId,
                                         @Valid @RequestBody RegistroChavePixDTO request) {
        try {
            return pixService.registrarChavePix(walletId, request.getTipoChavePix(), request.getValorChave());
        } catch (Exception ex) {
            throw new InternalServerErrorException("Erro: " + ex.getMessage());
        }
    }

    @PostMapping("/pix/transferencias")
    @ResponseStatus(HttpStatus.OK)
    public TransferenciaPixDTO transferir(@RequestHeader(name = "Idempotency-Key") String chaveIdempotencia,
                                          @Valid @RequestBody RequisicaoPixTransferenciaDTO request) {
        try {
            return pixService.transferir(chaveIdempotencia, request.getCarteiraIdOrigem(), request.getChavePixDestino(),
                    request.getValor());
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerErrorException(ex.getMessage());
        }
    }

    @PostMapping("/pix/webhooks")
    @ResponseStatus(HttpStatus.OK)
    public TransferenciaPixDTO processarWebhook(@Valid @RequestBody RequisicaoPixWebhookDTO request) {
        try {
            return pixService.processarWebhook(request);
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ex.getMessage());
        }
    }
}