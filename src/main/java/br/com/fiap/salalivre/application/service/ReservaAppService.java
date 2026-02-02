package br.com.fiap.salalivre.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.domain.event.ReservaAlteradaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCanceladaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCriadaEvent;
import br.com.fiap.salalivre.domain.exception.ConflitoDeHorarioException;
import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.ReservaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.UsuarioMapper;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.UsuarioJpaRepository;

@Service
public class ReservaAppService {
    private final SalaJpaRepository salaRepositorio;
    private final UsuarioJpaRepository usuarioRepositorio;
    private final ReservaJpaRepository reservaRepositorio;
    private final NotificacaoService notificacaoService;
    private final ReservaMapper reservaMapper = new ReservaMapper();
    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    public ReservaAppService(SalaJpaRepository salaRepositorio,
                             UsuarioJpaRepository usuarioRepositorio,
                             ReservaJpaRepository reservaRepositorio,
                             NotificacaoService notificacaoService) {
        this.salaRepositorio = salaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public Reserva criarReserva(UUID usuarioId, UUID salaId, PeriodoReserva periodo) {
        if (usuarioId == null || salaId == null || periodo == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para criar reserva nao informados.");
        }
        validarSalaExistente(salaId);
        validarUsuarioExistente(usuarioId);
        validarConflitoHorario(salaId, periodo, null);

        Reserva reserva = new Reserva(UUID.randomUUID(), salaId, usuarioId, periodo);
        ReservaEntity salva = reservaRepositorio.save(reservaMapper.toEntity(reserva));

        ReservaCriadaEvent evento = new ReservaCriadaEvent(
                salva.getId(),
                salva.getSalaId(),
                salva.getUsuarioId(),
                salva.getInicio(),
                salva.getFim()
        );
        notificacaoService.notificarReservaCriada(evento);
        return reservaMapper.toDomain(salva);
    }

    @Transactional
    public Reserva cancelarReserva(UUID reservaId, UUID solicitanteUsuarioId) {
        if (reservaId == null || solicitanteUsuarioId == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para cancelar reserva nao informados.");
        }
        ReservaEntity reservaEntity = buscarReservaEntity(reservaId);
        UsuarioEntity solicitanteEntity = buscarUsuarioEntity(solicitanteUsuarioId);

        Reserva reserva = reservaMapper.toDomain(reservaEntity);
        Usuario solicitante = usuarioMapper.toDomain(solicitanteEntity);
        reserva.cancelar(solicitante);

        reservaMapper.atualizarEntity(reserva, reservaEntity);
        ReservaEntity salva = reservaRepositorio.save(reservaEntity);

        ReservaCanceladaEvent evento = new ReservaCanceladaEvent(
                salva.getId(),
                salva.getSalaId(),
                salva.getUsuarioId(),
                salva.getInicio(),
                salva.getFim()
        );
        notificacaoService.notificarReservaCancelada(evento);
        return reservaMapper.toDomain(salva);
    }

    @Transactional
    public Reserva alterarReserva(UUID reservaId, PeriodoReserva novoPeriodo) {
        if (reservaId == null || novoPeriodo == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para alterar reserva nao informados.");
        }
        ReservaEntity reservaEntity = buscarReservaEntity(reservaId);
        validarSalaExistente(reservaEntity.getSalaId());
        validarConflitoHorario(reservaEntity.getSalaId(), novoPeriodo, reservaId);

        Reserva reserva = reservaMapper.toDomain(reservaEntity);
        reserva.alterarPeriodo(novoPeriodo);

        reservaMapper.atualizarEntity(reserva, reservaEntity);
        ReservaEntity salva = reservaRepositorio.save(reservaEntity);

        ReservaAlteradaEvent evento = new ReservaAlteradaEvent(
                salva.getId(),
                salva.getSalaId(),
                salva.getUsuarioId(),
                salva.getInicio(),
                salva.getFim()
        );
        notificacaoService.notificarReservaAlterada(evento);
        return reservaMapper.toDomain(salva);
    }

    private void validarSalaExistente(UUID salaId) {
        if (!salaRepositorio.existsById(salaId)) {
            throw new EntidadeNaoEncontradaException("Sala nao encontrada.");
        }
    }

    private void validarUsuarioExistente(UUID usuarioId) {
        if (!usuarioRepositorio.existsById(usuarioId)) {
            throw new EntidadeNaoEncontradaException("Usuario nao encontrado.");
        }
    }

    private void validarConflitoHorario(UUID salaId, PeriodoReserva periodo, UUID reservaIgnorada) {
        boolean temConflito;
        if (reservaIgnorada == null) {
            temConflito = !reservaRepositorio.findConflitos(
                    salaId,
                    periodo.inicio(),
                    periodo.fim(),
                    StatusReserva.CANCELADA
            ).isEmpty();
        } else {
            temConflito = !reservaRepositorio.findConflitosExcluindoReserva(
                    salaId,
                    periodo.inicio(),
                    periodo.fim(),
                    StatusReserva.CANCELADA,
                    reservaIgnorada
            ).isEmpty();
        }
        if (temConflito) {
            throw new ConflitoDeHorarioException("Conflito de horario para a sala.");
        }
    }

    private ReservaEntity buscarReservaEntity(UUID reservaId) {
        return reservaRepositorio.findById(reservaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Reserva nao encontrada."));
    }

    private UsuarioEntity buscarUsuarioEntity(UUID usuarioId) {
        return usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario nao encontrado."));
    }

}
