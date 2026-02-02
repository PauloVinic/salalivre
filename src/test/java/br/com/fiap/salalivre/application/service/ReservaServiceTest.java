package br.com.fiap.salalivre.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.application.repository.ReservaRepositorioMemoria;
import br.com.fiap.salalivre.application.repository.SalaRepositorioMemoria;
import br.com.fiap.salalivre.application.repository.UsuarioRepositorioMemoria;
import br.com.fiap.salalivre.domain.exception.PermissaoNegadaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;

class ReservaServiceTest {
    private SalaRepositorioMemoria salaRepositorio;
    private UsuarioRepositorioMemoria usuarioRepositorio;
    private ReservaRepositorioMemoria reservaRepositorio;
    private ReservaService reservaService;

    @BeforeEach
    void setUp() {
        salaRepositorio = new SalaRepositorioMemoria();
        usuarioRepositorio = new UsuarioRepositorioMemoria();
        reservaRepositorio = new ReservaRepositorioMemoria();
        reservaService = new ReservaService(salaRepositorio, usuarioRepositorio, reservaRepositorio, new NotificacaoService());
    }

    @Test
    void criarReservaComSalaInexistenteDeveLancarExcecaoDominio() {
        UUID usuarioId = UUID.randomUUID();
        criarUsuario(usuarioId, TipoUsuario.COMUM, "usuario@sala.com");

        PeriodoReserva periodo = periodoPadrao();
        assertThrows(RegraDeNegocioException.class,
                () -> reservaService.criarReserva(usuarioId, UUID.randomUUID(), periodo));
    }

    @Test
    void criarReservaComUsuarioInexistenteDeveLancarExcecaoDominio() {
        UUID salaId = UUID.randomUUID();
        criarSala(salaId);

        PeriodoReserva periodo = periodoPadrao();
        assertThrows(RegraDeNegocioException.class,
                () -> reservaService.criarReserva(UUID.randomUUID(), salaId, periodo));
    }

    @Test
    void criarReservaSemConflitoDeveCriarConfirmada() {
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(usuarioId, TipoUsuario.COMUM, "dono@sala.com");

        PeriodoReserva periodo = periodoPadrao();
        Reserva reserva = reservaService.criarReserva(usuarioId, salaId, periodo);

        assertNotNull(reserva.getId());
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
    }

    @Test
    void criarReservaComConflitoDeveLancarExcecao() {
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(usuarioId, TipoUsuario.COMUM, "dono@sala.com");

        Reserva existente = reservaService.criarReserva(usuarioId, salaId, periodoPadrao());
        assertNotNull(existente);

        PeriodoReserva periodoConflitante = new PeriodoReserva(
                LocalDateTime.of(2026, 1, 10, 9, 30),
                LocalDateTime.of(2026, 1, 10, 10, 30)
        );

        assertThrows(RegraDeNegocioException.class,
                () -> reservaService.criarReserva(usuarioId, salaId, periodoConflitante));
    }

    @Test
    void cancelarReservaPorComumNaoDonoDeveLancarPermissaoNegada() {
        UUID salaId = UUID.randomUUID();
        UUID donoId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(donoId, TipoUsuario.COMUM, "dono@sala.com");
        criarUsuario(outroId, TipoUsuario.COMUM, "outro@sala.com");

        Reserva reserva = reservaService.criarReserva(donoId, salaId, periodoPadrao());

        assertThrows(PermissaoNegadaException.class,
                () -> reservaService.cancelarReserva(reserva.getId(), outroId));
    }

    @Test
    void cancelarReservaPeloProprioUsuarioDeveAtualizarStatusParaCancelada() {
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(usuarioId, TipoUsuario.COMUM, "dono@sala.com");

        Reserva reserva = reservaService.criarReserva(usuarioId, salaId, periodoPadrao());

        reservaService.cancelarReserva(reserva.getId(), usuarioId);

        Reserva atualizada = reservaRepositorio.buscarPorId(reserva.getId());
        assertEquals(StatusReserva.CANCELADA, atualizada.getStatus());
    }

    @Test
    void cancelarReservaPorAdminDeveAtualizarStatusParaCancelada() {
        UUID salaId = UUID.randomUUID();
        UUID donoId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(donoId, TipoUsuario.COMUM, "dono@sala.com");
        criarUsuario(adminId, TipoUsuario.ADMIN, "admin@sala.com");

        Reserva reserva = reservaService.criarReserva(donoId, salaId, periodoPadrao());

        reservaService.cancelarReserva(reserva.getId(), adminId);

        Reserva atualizada = reservaRepositorio.buscarPorId(reserva.getId());
        assertEquals(StatusReserva.CANCELADA, atualizada.getStatus());
    }

    @Test
    void alterarReservaParaPeriodoConflitanteDeveLancarExcecao() {
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(usuarioId, TipoUsuario.COMUM, "dono@sala.com");

        Reserva reserva = criarReservaExistente(UUID.randomUUID(), salaId, usuarioId,
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 10, 9, 0),
                        LocalDateTime.of(2026, 1, 10, 10, 0)
                ));

        criarReservaExistente(UUID.randomUUID(), salaId, usuarioId,
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 10, 10, 30),
                        LocalDateTime.of(2026, 1, 10, 11, 30)
                ));

        PeriodoReserva periodoConflitante = new PeriodoReserva(
                LocalDateTime.of(2026, 1, 10, 10, 0),
                LocalDateTime.of(2026, 1, 10, 11, 0)
        );

        assertThrows(RegraDeNegocioException.class,
                () -> reservaService.alterarReserva(reserva.getId(), periodoConflitante));
    }

    @Test
    void alterarReservaParaPeriodoValidoDeveAtualizarStatusEAjustarData() throws InterruptedException {
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        criarSala(salaId);
        criarUsuario(usuarioId, TipoUsuario.COMUM, "dono@sala.com");

        Reserva reserva = criarReservaExistente(UUID.randomUUID(), salaId, usuarioId,
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 10, 9, 0),
                        LocalDateTime.of(2026, 1, 10, 10, 0)
                ));

        LocalDateTime atualizadoAntes = reserva.getAtualizadoEm();
        Thread.sleep(10);

        PeriodoReserva novoPeriodo = new PeriodoReserva(
                LocalDateTime.of(2026, 1, 10, 11, 0),
                LocalDateTime.of(2026, 1, 10, 12, 0)
        );

        Reserva alterada = reservaService.alterarReserva(reserva.getId(), novoPeriodo);

        assertEquals(StatusReserva.ALTERADA, alterada.getStatus());
        assertEquals(novoPeriodo, alterada.getPeriodo());
        assertTrue(alterada.getAtualizadoEm().isAfter(atualizadoAntes));
    }

    private Sala criarSala(UUID salaId) {
        Sala sala = new Sala(salaId, "Sala Azul", 10, "Andar 1", List.of("Projetor"), true);
        salaRepositorio.salvar(sala);
        return sala;
    }

    private Usuario criarUsuario(UUID usuarioId, TipoUsuario tipo, String email) {
        Usuario usuario = new Usuario(usuarioId, "Usuario", email, tipo);
        usuarioRepositorio.salvar(usuario);
        return usuario;
    }

    private Reserva criarReservaExistente(UUID reservaId, UUID salaId, UUID usuarioId, PeriodoReserva periodo) {
        Reserva reserva = new Reserva(reservaId, salaId, usuarioId, periodo);
        reservaRepositorio.salvar(reserva);
        return reserva;
    }

    private PeriodoReserva periodoPadrao() {
        return new PeriodoReserva(
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );
    }
}
