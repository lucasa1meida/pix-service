package com.pixservice.carteira.controller;

import com.pixservice.carteira.dto.CarteiraDTO;
import com.pixservice.carteira.dto.MovimentacaoCarteiraDTO;
import com.pixservice.carteira.dto.SaldoDTO;
import com.pixservice.carteira.service.CarteiraService;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.comum.excecoes.InternalServerErrorException;
import com.pixservice.comum.excecoes.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/carteiras")
@RequiredArgsConstructor
@Log4j2
public class CarteiraController {

    private final CarteiraService carteiraService;
    private static final String ERRO_SALVA_CARTEIRA = "Erro ao salvar carteira: ";
    private static final String ERRO_DEPOSITO_CARTEIRA = "Erro ao depositar na carteira: ";
    private static final String ERRO_SAQUE_CARTEIRA = "Erro ao sacar na carteira: ";
    private static final String ERRO_BUSCA_CARTEIRA = "Erro ao buscar carteira: ";

    @PostMapping("/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CarteiraDTO criar(@PathVariable("customerId") String customerId) {
        try {
            return carteiraService.criar(customerId);
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ERRO_SALVA_CARTEIRA + ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerErrorException(ERRO_SALVA_CARTEIRA + ex.getMessage());
        }
    }

    @PostMapping("/{carteiraId}/depositar")
    @ResponseStatus(HttpStatus.OK)
    public CarteiraDTO depositar(@PathVariable("carteiraId") UUID carteiraId, @RequestBody MovimentacaoCarteiraDTO movimentacaoCarteiraDTO) {
        try {
            return carteiraService.depositar(carteiraId, movimentacaoCarteiraDTO.getQuantidade());
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ERRO_DEPOSITO_CARTEIRA + ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerErrorException(ERRO_DEPOSITO_CARTEIRA + ex.getMessage());
        }
    }

    @PostMapping("/{carteiraId}/sacar")
    @ResponseStatus(HttpStatus.OK)
    public CarteiraDTO sacar(@PathVariable("carteiraId") UUID carteiraId, @RequestBody MovimentacaoCarteiraDTO movimentacaoCarteiraDTO) {
        try {
            return carteiraService.sacar(carteiraId, movimentacaoCarteiraDTO.getQuantidade());
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ERRO_SAQUE_CARTEIRA + ex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerErrorException(ERRO_SAQUE_CARTEIRA + ex.getMessage());
        }
    }

    @GetMapping("/{carteiraId}/saldo")
    public SaldoDTO obterSaldo(@PathVariable("carteiraId") UUID carteiraId, @RequestParam(name = "data", required = false) LocalDateTime data) {
        try {
            return carteiraService.obterSaldo(carteiraId, data);
        } catch (ExcecaoDeDominio ex) {
            throw new ExcecaoDeDominio(ERRO_BUSCA_CARTEIRA + ex.getMessage());
        } catch (NotFoundException nfex) {
            throw new NotFoundException("Dados não encontrados: " + nfex.getMessage());
        } catch (Exception ex) {
            throw new InternalServerErrorException(ERRO_BUSCA_CARTEIRA + ex.getMessage());
        }
    }
}