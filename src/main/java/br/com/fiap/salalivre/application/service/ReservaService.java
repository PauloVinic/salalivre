package br.com.fiap.salalivre.application.service;

import java.util.UUID;

import br.com.fiap.salalivre.application.repository.ReservaRepositorioMemoria;
import br.com.fiap.salalivre.application.repository.SalaRepositorioMemoria;
import br.com.fiap.salalivre.application.repository.UsuarioRepositorioMemoria;
import br.com.fiap.salalivre.domain.event.ReservaAlteradaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCanceladaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCriadaEvent;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;

public class ReservaService {
    private final SalaRepositorioMemoria salaRepositorio;
    private final UsuarioRepositorioMemoria usuarioRepositorio;
    private final ReservaRepositorioMemoria reservaRepositorio;
    private final NotificacaoService notificacaoService;

    public ReservaService() {
        this(new SalaRepositorioMemoria(), new UsuarioRepositorioMemoria(), new ReservaRepositorioMemoria(), new NotificacaoService());
    }

    public ReservaService(SalaRepositorioMemoria salaRepositorio,
                          UsuarioRepositorioMemoria usuarioRepositorio,
                          ReservaRepositorioMemoria reservaRepositorio,
                          NotificacaoService notificacaoService) {
        if (salaRepositorio == null || usuarioRepositorio == null || reservaRepositorio == null || notificacaoService == null) {
            throw new IllegalArgumentException("Dependencias obrigatorias nao informadas.");
        }
        this.salaRepositorio = salaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.notificacaoService = notificacaoService;
    }

    public Reserva criarReserva(UUID usuarioId, UUID salaId, PeriodoReserva periodo) {
        if (usuarioId == null || salaId == null || periodo == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para criar reserva nao informados.");
        }
        Sala sala = buscarSalaObrigatoria(salaId);
        Usuario usuario = buscarUsuarioObrigatorio(usuarioId);

        validarConflitoHorario(sala.getId(), periodo, null);

        Reserva reserva = new Reserva(UUID.randomUUID(), sala.getId(), usuario.getId(), periodo);
        reservaRepositorio.salvar(reserva);

        ReservaCriadaEvent evento = new ReservaCriadaEvent(
                reserva.getId(),
                reserva.getSalaId(),
                reserva.getUsuarioId(),
                reserva.getPeriodo().inicio(),
                reserva.getPeriodo().fim()
        );
        notificacaoService.notificarReservaCriada(evento);
        return reserva;
    }

    public void cancelarReserva(UUID reservaId, UUID solicitanteUsuarioId) {
        if (reservaId == null || solicitanteUsuarioId == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para cancelar reserva nao informados.");
        }
        Reserva reserva = buscarReservaObrigatoria(reservaId);
        Usuario solicitante = buscarUsuarioObrigatorio(solicitanteUsuarioId);

        reserva.cancelar(solicitante);
        reservaRepositorio.salvar(reserva);

        ReservaCanceladaEvent evento = new ReservaCanceladaEvent(
                reserva.getId(),
                reserva.getSalaId(),
                reserva.getUsuarioId(),
                reserva.getPeriodo().inicio(),
                reserva.getPeriodo().fim()
        );
        notificacaoService.notificarReservaCancelada(evento);
    }

    public Reserva alterarReserva(UUID reservaId, PeriodoReserva novoPeriodo) {
        if (reservaId == null || novoPeriodo == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para alterar reserva nao informados.");
        }
        Reserva reserva = buscarReservaObrigatoria(reservaId);
        buscarSalaObrigatoria(reserva.getSalaId());

        validarConflitoHorario(reserva.getSalaId(), novoPeriodo, reserva.getId());

        reserva.alterarPeriodo(novoPeriodo);
        reservaRepositorio.salvar(reserva);

        ReservaAlteradaEvent evento = new ReservaAlteradaEvent(
                reserva.getId(),
                reserva.getSalaId(),
                reserva.getUsuarioId(),
                reserva.getPeriodo().inicio(),
                reserva.getPeriodo().fim()
        );
        notificacaoService.notificarReservaAlterada(evento);
        return reserva;
    }

    private void validarConflitoHorario(UUID salaId, PeriodoReserva novoPeriodo, UUID reservaIgnorada) {
        for (Reserva existente : reservaRepositorio.listarPorSalaId(salaId)) {
            if (reservaIgnorada != null && existente.getId().equals(reservaIgnorada)) {
                continue;
            }
            if (existente.getStatus() == StatusReserva.CANCELADA) {
                continue;
            }
            if (haSobreposicao(novoPeriodo, existente.getPeriodo())) {
                throw new RegraDeNegocioException("Conflito de horario para a sala.");
            }
        }
    }

    private boolean haSobreposicao(PeriodoReserva novoPeriodo, PeriodoReserva periodoExistente) {
        return novoPeriodo.inicio().isBefore(periodoExistente.fim())
                && novoPeriodo.fim().isAfter(periodoExistente.inicio());
    }

    private Sala buscarSalaObrigatoria(UUID salaId) {
        Sala sala = salaRepositorio.buscarPorId(salaId);
        if (sala == null) {
            throw new RegraDeNegocioException("Sala nao encontrada.");
        }
        return sala;
    }

    private Usuario buscarUsuarioObrigatorio(UUID usuarioId) {
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new RegraDeNegocioException("Usuario nao encontrado.");
        }
        return usuario;
    }

    private Reserva buscarReservaObrigatoria(UUID reservaId) {
        Reserva reserva = reservaRepositorio.buscarPorId(reservaId);
        if (reserva == null) {
            throw new RegraDeNegocioException("Reserva nao encontrada.");
        }
        return reserva;
    }
}
