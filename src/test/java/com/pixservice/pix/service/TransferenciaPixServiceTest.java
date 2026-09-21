package com.pixservice.pix.service;

import com.ExtensoesDeTeste;
import com.pixservice.carteira.entity.Carteira;
import com.pixservice.carteira.repository.CarteiraRepository;
import com.pixservice.comum.excecoes.ExcecaoDeDominio;
import com.pixservice.pix.dto.TransferenciaPixDTO;
import com.pixservice.pix.entity.ChavePix;
import com.pixservice.pix.entity.RegistroIdempotencia;
import com.pixservice.pix.entity.TransferenciaPix;
import com.pixservice.pix.enums.TipoChavePix;
import com.pixservice.pix.repository.ChavePixRepository;
import com.pixservice.pix.repository.RegistroIdempotenciaRepository;
import com.pixservice.pix.repository.TransferenciaPixRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtensionMethod({ExtensoesDeTeste.class})
class TransferenciaPixServiceTest {

    @Mock
    private CarteiraRepository carteiraRepository;

    @Mock
    private ChavePixRepository chavePixRepository;

    @Mock
    private RegistroIdempotenciaRepository registroIdempotenciaRepository;

    @Mock
    private TransferenciaPixRepository transferenciaPixRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TransferenciaPixService transferenciaPixService;

    private String chaveIdempotencia;
    private UUID carteiraIdOrigem;
    private String chavePixDestino;
    private BigDecimal valorTransferencia;
    private String payloadHash;
    private Carteira carteiraOrigem;
    private Carteira carteiraDestino;
    private ChavePix chavePix;
    private UUID carteiraIdDestino;

    @BeforeEach
    @DisplayName("Deve inicializar dados comuns para TransferenciaPixServiceTest")
    void setUp() {
        chaveIdempotencia = "idempotency-key-123";
        carteiraIdOrigem = UUID.randomUUID();
        carteiraIdDestino = UUID.randomUUID();
        chavePixDestino = "usuario@example.com";
        valorTransferencia = new BigDecimal("100.00");
        payloadHash = "abc123def456";
        carteiraOrigem = new Carteira("customer-origem");
        carteiraDestino = new Carteira("customer-destino");
        chavePix = new ChavePix(carteiraIdDestino, TipoChavePix.EMAIL, chavePixDestino);
    }

    @Test
    @DisplayName("Deve retornar null quando não existir registro de idempotência")
    void deveRetornarNullQuandoNaoExistirRegistroIdempotencia() {
        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(null);

        TransferenciaPixDTO resultado = transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash);

        assertNull(resultado);
        verify(transferenciaPixRepository, never()).findByReferencia(any());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando payloadHash armazenado for diferente")
    void deveRetornarExcecaoQuandoPayloadHashForDiferente() {
        RegistroIdempotencia registro = new RegistroIdempotencia(chaveIdempotencia, "referencia-123", "hash-diferente");
        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(registro);

        assertThrows(ExcecaoDeDominio.class, () -> transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash))
                .comMensagemDeErro("A chave de idempotência já está sendo usado por outro paylod.");
    }

    @Test
    @DisplayName("Deve retornar null quando referência no registro for nula")
    void deveRetornarNullQuandoReferenciaForNula() {
        RegistroIdempotencia registro = mock(RegistroIdempotencia.class);
        when(registro.getPayloadHash()).thenReturn(payloadHash);
        when(registro.getReferencia()).thenReturn(null);
        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(registro);

        TransferenciaPixDTO resultado = transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash);

        assertNull(resultado);
        verify(transferenciaPixRepository, never()).findByReferencia(any());
    }

    @Test
    @DisplayName("Deve retornar null quando referência no registro for vazia")
    void deveRetornarNullQuandoReferenciaForVazia() {
        RegistroIdempotencia registro = mock(RegistroIdempotencia.class);
        when(registro.getPayloadHash()).thenReturn(payloadHash);
        when(registro.getReferencia()).thenReturn("   ");
        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(registro);

        TransferenciaPixDTO resultado = transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash);

        assertNull(resultado);
        verify(transferenciaPixRepository, never()).findByReferencia(any());
    }

    @Test
    @DisplayName("Deve retornar transferência existente quando registro possuir referência")
    void deveRetornarTransferenciaExistenteQuandoRegistroTiverReferencia() {
        String referencia = "E12345678202501151030123456789";
        RegistroIdempotencia registro = new RegistroIdempotencia(chaveIdempotencia, referencia, payloadHash);
        TransferenciaPix transferencia = new TransferenciaPix(referencia, carteiraIdOrigem, carteiraIdDestino,
                chavePixDestino, valorTransferencia);
        TransferenciaPixDTO transferenciaDTO = new TransferenciaPixDTO();

        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(registro);
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.of(transferencia));
        when(modelMapper.map(transferencia, TransferenciaPixDTO.class)).thenReturn(transferenciaDTO);

        TransferenciaPixDTO resultado = transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash);

        assertNotNull(resultado);
        assertEquals(transferenciaDTO, resultado);
        verify(transferenciaPixRepository).findByReferencia(referencia);
        verify(modelMapper).map(transferencia, TransferenciaPixDTO.class);
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando transferência não for encontrada pela referência")
    void deveLancarExcecaoQuandoTransferenciaNaoForEncontrada() {
        String referencia = "E12345678202501151030123456789";
        RegistroIdempotencia registro = new RegistroIdempotencia(chaveIdempotencia, referencia, payloadHash);

        when(registroIdempotenciaRepository.findByChaveIdempotencia(chaveIdempotencia)).thenReturn(registro);
        when(transferenciaPixRepository.findByReferencia(referencia)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> transferenciaPixService.obterTransferenciaPixExistente(chaveIdempotencia, payloadHash))
                .comMensagemDeErro("Transferência Pix não encontrada para a referência " + referencia);
    }

    @Test
    @DisplayName("Deve criar nova transferência Pix com sucesso")
    void deveCriarNovaTransferenciaPixComSucesso() {
        when(carteiraRepository.findWithLockingById(carteiraIdOrigem)).thenReturn(Optional.of(carteiraOrigem));
        when(chavePixRepository.findByValorChaveAndAtivoTrue(chavePixDestino)).thenReturn(Optional.of(chavePix));
        when(carteiraRepository.findWithLockingById(carteiraIdDestino)).thenReturn(Optional.of(carteiraDestino));
        when(transferenciaPixRepository.save(any(TransferenciaPix.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(registroIdempotenciaRepository.saveAndFlush(any(RegistroIdempotencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferenciaPix resultado = transferenciaPixService.obterTransferenciaPixNova(chaveIdempotencia, carteiraIdOrigem,
                chavePixDestino, valorTransferencia, payloadHash);

        assertNotNull(resultado);
        assertEquals(chavePixDestino, resultado.getChavePix());
        assertEquals(valorTransferencia, resultado.getValor());
        assertEquals(carteiraDestino.getId(), resultado.getCarteiraIdOrigem());
        assertEquals(carteiraOrigem.getId(), resultado.getCarteiraIdDestino());

        ArgumentCaptor<TransferenciaPix> transferenciaCaptor = ArgumentCaptor.forClass(TransferenciaPix.class);
        verify(transferenciaPixRepository).save(transferenciaCaptor.capture());
        assertNotNull(transferenciaCaptor.getValue().getReferencia());

        ArgumentCaptor<RegistroIdempotencia> registroCaptor = ArgumentCaptor.forClass(RegistroIdempotencia.class);
        verify(registroIdempotenciaRepository).saveAndFlush(registroCaptor.capture());
        assertEquals(chaveIdempotencia, registroCaptor.getValue().getChaveIdempotencia());
        assertEquals(payloadHash, registroCaptor.getValue().getPayloadHash());
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteira de origem não for encontrada")
    void deveLancarExcecaoQuandoCarteiraOrigemNaoForEncontrada() {
        when(carteiraRepository.findWithLockingById(carteiraIdOrigem)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> transferenciaPixService.obterTransferenciaPixNova(chaveIdempotencia,
                carteiraIdOrigem, chavePixDestino, valorTransferencia, payloadHash))
                .comMensagemDeErro("Carteira de origem não encontrada: " + carteiraIdOrigem);
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando chave Pix de destino não for encontrada")
    void deveLancarExcecaoQuandoChavePixNaoForEncontrada() {
        when(carteiraRepository.findWithLockingById(carteiraIdOrigem)).thenReturn(Optional.of(carteiraOrigem));
        when(chavePixRepository.findByValorChaveAndAtivoTrue(chavePixDestino)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> transferenciaPixService.obterTransferenciaPixNova(chaveIdempotencia,
                carteiraIdOrigem, chavePixDestino, valorTransferencia, payloadHash))
                .comMensagemDeErro("Chave pix de destino não foi encontrada ou está inativa.");
    }

    @Test
    @DisplayName("Deve lançar ExcecaoDeDominio quando carteira de destino não for encontrada")
    void deveLancarExcecaoQuandoCarteiraDestinoNaoForEncontrada() {
        when(carteiraRepository.findWithLockingById(carteiraIdOrigem)).thenReturn(Optional.of(carteiraOrigem));
        when(chavePixRepository.findByValorChaveAndAtivoTrue(chavePixDestino)).thenReturn(Optional.of(chavePix));
        when(carteiraRepository.findWithLockingById(carteiraIdDestino)).thenReturn(Optional.empty());

        assertThrows(ExcecaoDeDominio.class, () -> transferenciaPixService.obterTransferenciaPixNova(chaveIdempotencia,
                carteiraIdOrigem, chavePixDestino, valorTransferencia, payloadHash))
                .comMensagemDeErro("Carteira de destino não encontrada: " + carteiraIdDestino);
    }
}