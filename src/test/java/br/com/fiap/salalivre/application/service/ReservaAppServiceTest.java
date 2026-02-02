package br.com.fiap.salalivre.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.salalivre.domain.exception.ConflitoDeHorarioException;
import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.PermissaoNegadaException;
import br.com.fiap.salalivre.domain.event.ReservaAlteradaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCanceladaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCriadaEvent;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.UsuarioJpaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaAppServiceTest {
    private static final UUID SALA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID RESERVA_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID OUTRO_USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 10, 9, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 1, 10, 10, 0);
    private static final LocalDateTime CRIADO_EM = LocalDateTime.of(2026, 1, 1, 8, 0);
    private static final LocalDateTime ATUALIZADO_EM = LocalDateTime.of(2026, 1, 1, 8, 0);

    @Mock
    private SalaJpaRepository salaRepositorio;

    @Mock
    private UsuarioJpaRepository usuarioRepositorio;

    @Mock
    private ReservaJpaRepository reservaRepositorio;

    @Mock
    private NotificacaoService notificacaoService;

    private ReservaAppService reservaAppService;

    @BeforeEach
    void setUp() {
        reservaAppService = new ReservaAppService(salaRepositorio, usuarioRepositorio, reservaRepositorio, notificacaoService);
    }

    @Test
    void criarReserva_deveCriarQuandoNaoHaConflito() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

        when(salaRepositorio.existsById(SALA_ID)).thenReturn(true);
        when(usuarioRepositorio.existsById(USUARIO_ID)).thenReturn(true);

        when(reservaRepositorio.findConflitos(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA))
                .thenReturn(List.of());

        ReservaEntity salva = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);
        when(reservaRepositorio.save(any(ReservaEntity.class))).thenReturn(salva);

        Reserva reserva = reservaAppService.criarReserva(USUARIO_ID, SALA_ID, periodo);

        assertNotNull(reserva);
        assertEquals(RESERVA_ID, reserva.getId());
        assertEquals(SALA_ID, reserva.getSalaId());
        assertEquals(USUARIO_ID, reserva.getUsuarioId());
        assertEquals(periodo, reserva.getPeriodo());
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        verify(reservaRepositorio).findConflitos(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA);
        verify(reservaRepositorio).save(any(ReservaEntity.class));
        verify(notificacaoService).notificarReservaCriada(any(ReservaCriadaEvent.class));
    }

    @Test
    void criarReserva_deveLancarConflitoQuandoHaConflito() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

        when(salaRepositorio.existsById(SALA_ID)).thenReturn(true);
        when(usuarioRepositorio.existsById(USUARIO_ID)).thenReturn(true);
        when(reservaRepositorio.findConflitos(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA))
                .thenReturn(List.of(reservaEntity(UUID.randomUUID(), SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA)));

        assertThrows(ConflitoDeHorarioException.class,
                () -> reservaAppService.criarReserva(USUARIO_ID, SALA_ID, periodo));

        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void criarReserva_deveLancarEntidadeNaoEncontradaQuandoSalaNaoExiste() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

        when(salaRepositorio.existsById(SALA_ID)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> reservaAppService.criarReserva(USUARIO_ID, SALA_ID, periodo));

        verify(usuarioRepositorio, never()).existsById(any(UUID.class));
        verify(reservaRepositorio, never()).findConflitos(any(UUID.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any(StatusReserva.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void criarReserva_deveLancarEntidadeNaoEncontradaQuandoUsuarioNaoExiste() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);

        when(salaRepositorio.existsById(SALA_ID)).thenReturn(true);
        when(usuarioRepositorio.existsById(USUARIO_ID)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> reservaAppService.criarReserva(USUARIO_ID, SALA_ID, periodo));

        verify(reservaRepositorio, never()).findConflitos(any(UUID.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any(StatusReserva.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void alterarReserva_deveAlterarQuandoMesmoPeriodoSemConflito() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);
        ReservaEntity existente = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);

        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.of(existente));
        when(salaRepositorio.existsById(SALA_ID)).thenReturn(true);

        when(reservaRepositorio.findConflitosExcluindoReserva(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA, RESERVA_ID))
                .thenReturn(List.of());
        when(reservaRepositorio.save(any(ReservaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaAppService.alterarReserva(RESERVA_ID, periodo);

        assertNotNull(reserva);
        assertEquals(RESERVA_ID, reserva.getId());
        assertEquals(SALA_ID, reserva.getSalaId());
        assertEquals(USUARIO_ID, reserva.getUsuarioId());
        assertEquals(periodo, reserva.getPeriodo());
        verify(reservaRepositorio).findConflitosExcluindoReserva(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA, RESERVA_ID);
        verify(reservaRepositorio).save(any(ReservaEntity.class));
        verify(notificacaoService).notificarReservaAlterada(any(ReservaAlteradaEvent.class));
    }

    @Test
    void alterarReserva_deveLancarConflitoQuandoOutraReservaConflita() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);
        ReservaEntity existente = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);

        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.of(existente));
        when(salaRepositorio.existsById(SALA_ID)).thenReturn(true);
        when(reservaRepositorio.findConflitosExcluindoReserva(SALA_ID, INICIO, FIM, StatusReserva.CANCELADA, RESERVA_ID))
                .thenReturn(List.of(reservaEntity(UUID.randomUUID(), SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA)));

        assertThrows(ConflitoDeHorarioException.class,
                () -> reservaAppService.alterarReserva(RESERVA_ID, periodo));

        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void alterarReserva_deveLancarEntidadeNaoEncontradaQuandoSalaNaoExiste() {
        PeriodoReserva periodo = new PeriodoReserva(INICIO, FIM);
        ReservaEntity existente = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);

        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.of(existente));
        when(salaRepositorio.existsById(SALA_ID)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> reservaAppService.alterarReserva(RESERVA_ID, periodo));

        verify(reservaRepositorio, never()).findConflitosExcluindoReserva(any(UUID.class), any(LocalDateTime.class),
                any(LocalDateTime.class), any(StatusReserva.class), any(UUID.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void cancelarReserva_deveCancelarQuandoSolicitanteAdmin() {
        ReservaEntity existente = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);
        UsuarioEntity admin = usuario(ADMIN_ID, TipoUsuario.ADMIN);

        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findById(ADMIN_ID)).thenReturn(Optional.of(admin));
        when(reservaRepositorio.save(any(ReservaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaAppService.cancelarReserva(RESERVA_ID, ADMIN_ID);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        verify(reservaRepositorio).save(any(ReservaEntity.class));
        verify(notificacaoService).notificarReservaCancelada(any(ReservaCanceladaEvent.class));
    }

    @Test
    void cancelarReserva_deveLancarPermissaoNegadaQuandoNaoAdminENaoDono() {
        ReservaEntity existente = reservaEntity(RESERVA_ID, SALA_ID, USUARIO_ID, INICIO, FIM, StatusReserva.CONFIRMADA);
        UsuarioEntity solicitante = usuario(OUTRO_USUARIO_ID, TipoUsuario.COMUM);

        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.of(existente));
        when(usuarioRepositorio.findById(OUTRO_USUARIO_ID)).thenReturn(Optional.of(solicitante));

        assertThrows(PermissaoNegadaException.class,
                () -> reservaAppService.cancelarReserva(RESERVA_ID, OUTRO_USUARIO_ID));

        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void deveLancarEntidadeNaoEncontradaQuandoReservaNaoExiste() {
        when(reservaRepositorio.findById(RESERVA_ID)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> reservaAppService.cancelarReserva(RESERVA_ID, USUARIO_ID));

        verify(usuarioRepositorio, never()).findById(any(UUID.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
        verifyNoInteractions(notificacaoService);
    }

    private static UsuarioEntity usuario(UUID usuarioId, TipoUsuario tipo) {
        return UsuarioEntity.builder()
                .id(usuarioId)
                .nome("Usuario")
                .email("usuario@sala.com")
                .tipo(tipo)
                .build();
    }

    private static ReservaEntity reservaEntity(UUID reservaId,
                                               UUID salaId,
                                               UUID usuarioId,
                                               LocalDateTime inicio,
                                               LocalDateTime fim,
                                               StatusReserva status) {
        return ReservaEntity.builder()
                .id(reservaId)
                .salaId(salaId)
                .usuarioId(usuarioId)
                .inicio(inicio)
                .fim(fim)
                .status(status)
                .criadoEm(CRIADO_EM)
                .atualizadoEm(ATUALIZADO_EM)
                .build();
    }
}
