package com.pixservice.carteira.service;

import com.pixservice.carteira.dto.CarteiraDTO;
import com.pixservice.carteira.dto.SaldoDTO;
import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.NotFoundException;
import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import com.pixservice.historico_transacao.repository.HistoricoTransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteiraServiceTest {

    @Mock
    private CarteiraRepository carteiraRepository;

    @Mock
    private HistoricoTransacaoRepository historicoTransacaoRepository;

    @InjectMocks
    private CarteiraService carteiraService;

    private UUID carteiraId;
    private String customerId;
    private Carteira carteira;
    private BigDecimal valorDeposito;
    private BigDecimal valorSaque;

    @BeforeEach
    @DisplayName("Deve inicializar dados comuns para CarteiraServiceTest")
    void setUp() {
        carteiraId = UUID.randomUUID();
        customerId = "user-123";
        valorDeposito = new BigDecimal("100.00");
        valorSaque = new BigDecimal("50.00");
        carteira = new Carteira(customerId);
    }

    @Test
    @DisplayName("Deve criar carteira com sucesso via CarteiraService")
    void deveCriarCarteiraComSucesso() {
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarteiraDTO resultado = carteiraService.criar(customerId);

        assertNotNull(resultado);
        assertEquals(customerId, resultado.getCustomerId());
        assertEquals(BigDecimal.ZERO, resultado.getSaldo());
        assertNotNull(resultado.getCarteiraId());
        assertNotNull(resultado.getDataDeCriacao());
        assertNotNull(resultado.getDataDeAtualizacao());

        ArgumentCaptor<Carteira> carteiraCaptor = ArgumentCaptor.forClass(Carteira.class);
        verify(carteiraRepository).save(carteiraCaptor.capture());
        assertEquals(customerId, carteiraCaptor.getValue().getCustomerId());
    }

    @Test
    @DisplayName("Deve retornar saldo atual quando data for nula")
    void deveRetornarSaldoAtualQuandoDataForNula() {
        carteira.depositar(valorDeposito);
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(carteira));

        SaldoDTO resultado = carteiraService.obterSaldo(carteiraId, null);

        assertNotNull(resultado);
        assertEquals(valorDeposito, resultado.getSaldo());
        assertNotNull(resultado.getData());
        verify(historicoTransacaoRepository, never()).findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(any(), any());
    }

    @Test
    @DisplayName("Deve retornar saldo histórico quando data for informada")
    void deveRetornarSaldoHistoricoQuandoDataForInformada() {
        LocalDateTime dataConsulta = LocalDateTime.now().minusDays(1);
        HistoricoTransacao historico = HistoricoTransacao.depositar(carteiraId, valorDeposito,
                com.pixservice.historico_transacao.enums.TipoTransacao.DEPOSITO, null);
        when(historicoTransacaoRepository.findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(dataConsulta, carteiraId))
                .thenReturn(historico);

        SaldoDTO resultado = carteiraService.obterSaldo(carteiraId, dataConsulta);

        assertNotNull(resultado);
        assertEquals(valorDeposito, resultado.getSaldo());
        assertEquals(dataConsulta, resultado.getData());
        verify(carteiraRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando transação histórica não for encontrada")
    void deveLancarNotFoundExceptionQuandoTransacaoNaoForEncontrada() {
        LocalDateTime dataConsulta = LocalDateTime.now().minusDays(1);
        when(historicoTransacaoRepository.findFirstByDataTransacaoIsLessThanEqualAndCarteiraIdOrderByDataTransacaoDesc(dataConsulta, carteiraId))
                .thenReturn(null);

        assertThrows(NotFoundException.class, () -> carteiraService.obterSaldo(carteiraId, dataConsulta));
    }

    @Test
    @DisplayName("Deve depositar valor com sucesso atualizando saldo e histórico")
    void deveDepositarValorComSucesso() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(carteira));
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarteiraDTO resultado = carteiraService.depositar(carteiraId, valorDeposito);

        assertNotNull(resultado);
        assertEquals(valorDeposito, resultado.getSaldo());
        assertEquals(customerId, resultado.getCustomerId());

        ArgumentCaptor<Carteira> carteiraCaptor = ArgumentCaptor.forClass(Carteira.class);
        verify(carteiraRepository).save(carteiraCaptor.capture());
        assertEquals(valorDeposito, carteiraCaptor.getValue().getSaldo());

        ArgumentCaptor<HistoricoTransacao> historicoCaptor = ArgumentCaptor.forClass(HistoricoTransacao.class);
        verify(historicoTransacaoRepository).save(historicoCaptor.capture());
        assertEquals(carteiraId, historicoCaptor.getValue().getCarteiraId());
        assertEquals(valorDeposito, historicoCaptor.getValue().getValor());
    }

    @Test
    @DisplayName("Deve sacar valor com sucesso atualizando saldo e histórico")
    void deveSacarValorComSucesso() {
        carteira.depositar(new BigDecimal("100.00"));
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(carteira));
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarteiraDTO resultado = carteiraService.sacar(carteiraId, valorSaque);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("50.00"), resultado.getSaldo());
        assertEquals(customerId, resultado.getCustomerId());

        ArgumentCaptor<Carteira> carteiraCaptor = ArgumentCaptor.forClass(Carteira.class);
        verify(carteiraRepository).save(carteiraCaptor.capture());
        assertEquals(new BigDecimal("50.00"), carteiraCaptor.getValue().getSaldo());

        ArgumentCaptor<HistoricoTransacao> historicoCaptor = ArgumentCaptor.forClass(HistoricoTransacao.class);
        verify(historicoTransacaoRepository).save(historicoCaptor.capture());
        assertEquals(carteiraId, historicoCaptor.getValue().getCarteiraId());
        assertEquals(new BigDecimal("50.00"), historicoCaptor.getValue().getValor());
    }

    @Test
    @DisplayName("Deve lançar Error quando carteira não for encontrada ao obter saldo atual")
    void deveLancarErrorQuandoCarteiraNaoForEncontradaNoObterSaldo() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.empty());

        assertThrows(Error.class, () -> carteiraService.obterSaldo(carteiraId, null));
    }

    @Test
    @DisplayName("Deve lançar Error quando carteira não for encontrada ao depositar")
    void deveLancarErrorQuandoCarteiraNaoForEncontradaNoDepositar() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.empty());

        assertThrows(Error.class, () -> carteiraService.depositar(carteiraId, valorDeposito));
    }

    @Test
    @DisplayName("Deve lançar Error quando carteira não for encontrada ao sacar")
    void deveLancarErrorQuandoCarteiraNaoForEncontradaNoSacar() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.empty());

        assertThrows(Error.class, () -> carteiraService.sacar(carteiraId, valorSaque));
    }

    @Test
    @DisplayName("Deve mapear Carteira para CarteiraDTO corretamente")
    void deveMapearCarteiraParaDTOCorretamente() {
        carteira.depositar(valorDeposito);
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarteiraDTO resultado = carteiraService.criar(customerId);

        ArgumentCaptor<Carteira> carteiraCaptor = ArgumentCaptor.forClass(Carteira.class);
        verify(carteiraRepository).save(carteiraCaptor.capture());
        Carteira carteiraSalva = carteiraCaptor.getValue();

        assertEquals(carteiraSalva.getId(), resultado.getCarteiraId());
        assertEquals(carteiraSalva.getCustomerId(), resultado.getCustomerId());
        assertEquals(carteiraSalva.getSaldo(), resultado.getSaldo());
        assertEquals(carteiraSalva.getDataDeCriacao(), resultado.getDataDeCriacao());
        assertEquals(carteiraSalva.getDataDeAtualizacao(), resultado.getDataDeAtualizacao());
    }

    @Test
    @DisplayName("Deve criar histórico de transação com saldo atualizado após depósito")
    void deveCriarHistoricoTransacaoComSaldoAtualizadoAposDeposito() {
        BigDecimal valorInicial = new BigDecimal("50.00");
        carteira.depositar(valorInicial);
        BigDecimal valorDepositoAdicional = new BigDecimal("30.00");
        BigDecimal saldoEsperado = valorInicial.add(valorDepositoAdicional);

        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(carteira));
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carteiraService.depositar(carteiraId, valorDepositoAdicional);

        ArgumentCaptor<HistoricoTransacao> historicoCaptor = ArgumentCaptor.forClass(HistoricoTransacao.class);
        verify(historicoTransacaoRepository).save(historicoCaptor.capture());
        assertEquals(saldoEsperado, historicoCaptor.getValue().getValor());
    }

    @Test
    @DisplayName("Deve criar histórico de transação com saldo atualizado após saque")
    void deveCriarHistoricoTransacaoComSaldoAtualizadoAposSaque() {
        BigDecimal valorInicial = new BigDecimal("100.00");
        carteira.depositar(valorInicial);
        BigDecimal valorSaqueRealizado = new BigDecimal("40.00");
        BigDecimal saldoEsperado = valorInicial.subtract(valorSaqueRealizado);

        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(carteira));
        when(carteiraRepository.save(any(Carteira.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carteiraService.sacar(carteiraId, valorSaqueRealizado);

        ArgumentCaptor<HistoricoTransacao> historicoCaptor = ArgumentCaptor.forClass(HistoricoTransacao.class);
        verify(historicoTransacaoRepository).save(historicoCaptor.capture());
        assertEquals(saldoEsperado, historicoCaptor.getValue().getValor());
    }
}
