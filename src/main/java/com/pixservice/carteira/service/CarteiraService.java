package com.pixservice.carteira.service;

import com.pixservice.carteira.dto.CarteiraDTO;
import com.pixservice.carteira.dto.SaldoDTO;
import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.NotFoundException;
import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import com.pixservice.historico_transacao.enums.TipoTransacao;
import com.pixservice.historico_transacao.repository.HistoricoTransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Log4j2
@Service
public class CarteiraService implements ICarteiraService {

    private final CarteiraRepository carteiraRepository;
    private final HistoricoTransacaoRepository historicoTransacaoRepository;

    @Override
    public CarteiraDTO criar(String customerId) {
        Carteira carteira = new Carteira(customerId);
        carteiraRepository.save(carteira);
        log.info("Carteira Salvo com sucesso.");
        return mapearRespostaPor(carteira);
    }

    @Override
    public SaldoDTO obterSaldo(UUID carteiraId, LocalDateTime data) {
        if (data == null) {
            Carteira carteira = buscarCarteiraPor(carteiraId);
            return new SaldoDTO(carteira.getSaldo(), LocalDateTime.now());
        }

        HistoricoTransacao transacaoPorData = historicoTransacaoRepository.findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(data, carteiraId);
        if (transacaoPorData == null)
            throw new NotFoundException("Transação não encontrada.");

        return new SaldoDTO(transacaoPorData.getValor(), data);
    }

    @Override
    public CarteiraDTO depositar(UUID carteiraId, BigDecimal quantidade) {
        Carteira carteira = buscarCarteiraPor(carteiraId);
        carteira.depositar(quantidade);
        HistoricoTransacao historicoTransacao = HistoricoTransacao.depositar(carteiraId, carteira.getSaldo(), TipoTransacao.DEPOSITO, null);

        carteiraRepository.save(carteira);
        historicoTransacaoRepository.save(historicoTransacao);

        return mapearRespostaPor(carteira);
    }

    @Override
    public CarteiraDTO sacar(UUID carteiraId, BigDecimal quantidade) {
        Carteira carteira = buscarCarteiraPor(carteiraId);
        carteira.sacar(quantidade);
        HistoricoTransacao historicoTransacao = HistoricoTransacao.sacar(carteiraId, carteira.getSaldo(),
                TipoTransacao.SAQUE, null);

        carteiraRepository.save(carteira);
        historicoTransacaoRepository.save(historicoTransacao);

        return mapearRespostaPor(carteira);
    }

    private Carteira buscarCarteiraPor(UUID carteiraId) {
        return carteiraRepository.findById(carteiraId).orElseThrow(() -> new Error("Carteira não encontrada."));
    }

    private static CarteiraDTO mapearRespostaPor(Carteira carteira) {
        CarteiraDTO carteiraDTO = new CarteiraDTO();
        carteiraDTO.setCarteiraId(carteira.getId());
        carteiraDTO.setCustomerId(carteira.getCustomerId());
        carteiraDTO.setSaldo(carteira.getSaldo());
        carteiraDTO.setDataDeCriacao(carteira.getDataDeCriacao());
        carteiraDTO.setDataDeAtualizacao(carteira.getDataDeAtualizacao());
        return carteiraDTO;
    }
}