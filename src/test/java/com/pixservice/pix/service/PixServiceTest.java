package com.pixservice.pix.service;

import com.ExtensoesDeTeste;
import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.historico_transacao.entity.HistoricoTransacao;
import com.pixservice.historico_transacao.repository.HistoricoTransacaoRepository;
import com.pixservice.pix.dto.ChavePixDTO;
import com.pixservice.pix.dto.RequisicaoPixWebhookDTO;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.ChavePix;
import com.pixservice.pix.entity.EventoPixWebhook;
import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.enums.StatusTransferenciaPix;
import com.pixservice.pix.enums.TipoChavePix;
import com.pixservice.pix.enums.TipoEventoPixWebhook;
import com.pixservice.pix.repository.ChavePixRepository;
import com.pixservice.pix.repository.EventoWebhookRepository;
import com.pixservice.pix.repository.TransferenciaPixRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtensionMethod({ExtensoesDeTeste.class})
class PixServiceTest {

    @Mock
    private CarteiraRepository carteiraRepository;

    @Mock
    private ChavePixRepository chavePixRepository;

    @Mock
    private TransferenciaPixService transferenciaPixService;

    @Mock
    private EventoWebhookRepository eventoWebhookRepository;

    @Mock
    private TransferenciaPixRepository transferenciaPixRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @Mock
    private HistoricoTransacaoRepository historicoTransacaoRepository;

    @InjectMocks
    private PixService pixService;

    private UUID carteiraId;
    private TipoChavePix tipoChavePix;
    private String valorChave;
    private ChavePixDTO chavePixDTO;
    private String chaveIdempotencia;
    private UUID carteiraIdOrigem;
    private String chavePixDestino;
    private BigDecimal valorTransferencia;
    private TransferenciaPix transferenciaPix;
    private TransferenciaPixDTO transferenciaPixDTO;
    private RequisicaoPixWebhookDTO requisicaoWebhook;
    private String referencia;
    private String eventoId;
    private Carteira carteiraOrigem;
    private Carteira carteiraDestino;

    @BeforeEach
    @DisplayName("Deve inicializar dados comuns para PixServiceTest")
    void setUp() {
        carteiraId = UUID.randomUUID();
        tipoChavePix = TipoChavePix.EMAIL;
        valorChave = "usuario@example.com";
        chavePixDTO = new ChavePixDTO();

        chaveIdempotencia = "idempotency-key-123";
        carteiraIdOrigem = UUID.randomUUID();
        UUID carteiraIdDestino = UUID.randomUUID();
        chavePixDestino = "destino@example.com";
        valorTransferencia = new BigDecimal("100.00");
        transferenciaPix = new TransferenciaPix("E12345678202501151030123456789", carteiraIdOrigem,
                carteiraIdDestino, chavePixDestino, valorTransferencia);
        transferenciaPixDTO = new TransferenciaPixDTO();

        referencia = "E12345678202501151030123456789";
        eventoId = "evt-123e4567-e89b-12d3-a456-426614174000";
        requisicaoWebhook = new RequisicaoPixWebhookDTO();
        requisicaoWebhook.setReferencia(referencia);
        requisicaoWebhook.setEventoId(eventoId);
        requisicaoWebhook.setDataDeOcorrencia(LocalDateTime.now());

        carteiraOrigem = new Carteira("customer-origem");
        carteiraOrigem.depositar(new BigDecimal("200.00"));
        carteiraDestino = new Carteira("customer-destino");
    }

    @Test
    @DisplayName("Deve registrar uma nova chave Pix com sucesso")
    void deveRegistrarChavePixComSucesso() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.of(new Carteira("customer")));
        when(chavePixRepository.save(any(ChavePix.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(ChavePix.class), eq(ChavePixDTO.class))).thenReturn(chavePixDTO);

        ChavePixDTO resultado = pixService.registrarChavePix(carteiraId, tipoChavePix, valorChave);

        assertNotNull(resultado);
        assertEquals(chavePixDTO, resultado);

        ArgumentCaptor<ChavePix> chavePixCaptor = ArgumentCaptor.forClass(ChavePix.class);
        verify(chavePixRepository).save(chavePixCaptor.capture());
        assertEquals(carteiraId, chavePixCaptor.getValue().getCarteiraId());
        assertEquals(tipoChavePix, chavePixCaptor.getValue().getTipoChavePix());
        assertEquals(valorChave, chavePixCaptor.getValue().getValorChave());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando carteira não for encontrada ao registrar chave Pix")
    void deveLancarRuntimeExceptionQuandoCarteiraNaoForEncontradaNoRegistrarChavePix() {
        when(carteiraRepository.findById(carteiraId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pixService.registrarChavePix(carteiraId, tipoChavePix, valorChave))
                .comMensagemDeErro("Carteira não encontrada.");
    }

    @Test
    @DisplayName("Deve retornar transferência existente quando já houver registro para a chave de idempotência")
    void deveRetornarTransferenciaExistenteQuandoJaExistir() {
        when(transferenciaPixService.obterTransferenciaPixExistente(eq(chaveIdempotencia), anyString()))
                .thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.transferir(chaveIdempotencia, carteiraIdOrigem, chavePixDestino, valorTransferencia);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
        verify(transferenciaPixService, never()).obterTransferenciaPixNova(any(), any(), any(), any(), any());
        verify(meterRegistry, never()).counter(any());
    }

    @Test
    @DisplayName("Deve criar nova transferência Pix quando não existir previamente")
    void deveCriarNovaTransferenciaQuandoNaoExistir() {
        when(transferenciaPixService.obterTransferenciaPixExistente(eq(chaveIdempotencia), anyString()))
                .thenReturn(null);
        when(transferenciaPixService.obterTransferenciaPixNova(eq(chaveIdempotencia), eq(carteiraIdOrigem),
                eq(chavePixDestino), eq(valorTransferencia), anyString())).thenReturn(transferenciaPix);
        when(modelMapper.map(transferenciaPix, TransferenciaPixDTO.class)).thenReturn(transferenciaPixDTO);
        when(meterRegistry.counter("transferencias_pix_iniciadas")).thenReturn(counter);

        TransferenciaPixDTO resultado = pixService.transferir(chaveIdempotencia, carteiraIdOrigem, chavePixDestino, valorTransferencia);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
        verify(transferenciaPixService).obterTransferenciaPixNova(eq(chaveIdempotencia), eq(carteiraIdOrigem),
                eq(chavePixDestino), eq(valorTransferencia), anyString());
        verify(meterRegistry).counter("transferencias_pix_iniciadas");
        verify(counter).increment();
    }

    @Test
    @DisplayName("Deve retornar transferência existente quando evento de webhook já foi processado")
    void deveRetornarTransferenciaExistenteQuandoEventoJaFoiProcessado() {
        EventoPixWebhook eventoExistente = new EventoPixWebhook(eventoId, referencia,
                TipoEventoPixWebhook.CONFIRMADO, LocalDateTime.now(), LocalDateTime.now());
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.of(eventoExistente));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(modelMapper.map(transferenciaPix, TransferenciaPixDTO.class)).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
        verify(eventoWebhookRepository, never()).save(any());
        verify(transferenciaPixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando transferência não for encontrada no evento já existente")
    void deveLancarExcecaoQuandoTransferenciaNaoForEncontradaNoEventoExistente() {
        EventoPixWebhook eventoExistente = new EventoPixWebhook(eventoId, referencia,
                TipoEventoPixWebhook.CONFIRMADO, LocalDateTime.now(), LocalDateTime.now());
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.of(eventoExistente));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> pixService.processarWebhook(requisicaoWebhook))
                .comMensagemDeErro("Transferência Pix não encontrada para a referência " + referencia);
    }

    @Test
    @DisplayName("Deve processar webhook CONFIRMADO com sucesso, atualizando transferência e carteiras")
    void deveProcessarWebhookConfirmadoComSucesso() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(transferenciaPixRepository.save(any(TransferenciaPix.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdOrigem())).thenReturn(Optional.of(carteiraOrigem));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdDestino())).thenReturn(Optional.of(carteiraDestino));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TransferenciaPix.class), eq(TransferenciaPixDTO.class))).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);

        ArgumentCaptor<EventoPixWebhook> eventoCaptor = ArgumentCaptor.forClass(EventoPixWebhook.class);
        verify(eventoWebhookRepository).save(eventoCaptor.capture());
        assertEquals(eventoId, eventoCaptor.getValue().getEventoId());
        assertEquals(referencia, eventoCaptor.getValue().getEndToEndId());
        assertEquals(TipoEventoPixWebhook.CONFIRMADO, eventoCaptor.getValue().getTipo());

        ArgumentCaptor<TransferenciaPix> transferenciaCaptor = ArgumentCaptor.forClass(TransferenciaPix.class);
        verify(transferenciaPixRepository).save(transferenciaCaptor.capture());
        assertEquals(StatusTransferenciaPix.CONFIRMADO, transferenciaCaptor.getValue().getStatus());

        verify(historicoTransacaoRepository, times(2)).save(any(HistoricoTransacao.class));
        verify(carteiraRepository).findWithLockingById(transferenciaPix.getCarteiraIdOrigem());
        verify(carteiraRepository).findWithLockingById(transferenciaPix.getCarteiraIdDestino());
        assertEquals(new BigDecimal("100.00"), carteiraOrigem.getSaldo());
        assertEquals(new BigDecimal("100.00"), carteiraDestino.getSaldo());
        verify(carteiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar webhook REJEITADO com sucesso, atualizando transferência para REJEITADO")
    void deveProcessarWebhookRejeitadoComSucesso() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.REJEITADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(transferenciaPixRepository.save(any(TransferenciaPix.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TransferenciaPix.class), eq(TransferenciaPixDTO.class))).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);

        ArgumentCaptor<TransferenciaPix> transferenciaCaptor = ArgumentCaptor.forClass(TransferenciaPix.class);
        verify(transferenciaPixRepository).save(transferenciaCaptor.capture());
        assertEquals(StatusTransferenciaPix.REJEITADO, transferenciaCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando transferência não for encontrada em novo evento")
    void deveLancarExcecaoQuandoTransferenciaNaoForEncontradaNoNovoEvento() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> pixService.processarWebhook(requisicaoWebhook))
                .comMensagemDeErro("Transferência Pix não encontrada para a referência " + referencia);
    }

    @Test
    @DisplayName("Deve retornar transferência original quando carteira de origem não for encontrada ao confirmar")
    void deveRetornarTransferenciaOriginalQuandoCarteiraOrigemNaoForEncontrada() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdOrigem())).thenReturn(Optional.empty());
        when(modelMapper.map(transferenciaPix, TransferenciaPixDTO.class)).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
        verify(historicoTransacaoRepository, times(2)).save(any(HistoricoTransacao.class));
        verify(carteiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar transferência original quando carteira de destino não for encontrada ao confirmar")
    void deveRetornarTransferenciaOriginalQuandoCarteiraDestinoNaoForEncontradaAoProcessarWebhook() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdOrigem())).thenReturn(Optional.of(carteiraOrigem));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdDestino())).thenReturn(Optional.empty());
        when(modelMapper.map(transferenciaPix, TransferenciaPixDTO.class)).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
    }

    @Test
    @DisplayName("Deve retornar transferência original quando ocorrer erro ao confirmar")
    void deveRetornarTransferenciaOriginalQuandoErroAoConfirmar() {
        requisicaoWebhook.setTipoEvento(TipoEventoPixWebhook.CONFIRMADO);
        when(eventoWebhookRepository.findByEventoId(eventoId)).thenReturn(Optional.empty());
        when(eventoWebhookRepository.save(any(EventoPixWebhook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferenciaPix));
        when(historicoTransacaoRepository.save(any(HistoricoTransacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carteiraRepository.findWithLockingById(transferenciaPix.getCarteiraIdOrigem())).thenReturn(Optional.empty());
        when(modelMapper.map(transferenciaPix, TransferenciaPixDTO.class)).thenReturn(transferenciaPixDTO);

        TransferenciaPixDTO resultado = pixService.processarWebhook(requisicaoWebhook);

        assertNotNull(resultado);
        assertEquals(transferenciaPixDTO, resultado);
    }
}
