package br.com.fiap.salalivre.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.domain.event.ReservaAlteradaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCanceladaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCriadaEvent;
import br.com.fiap.salalivre.domain.exception.ConflitoDeHorarioException;
import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.PermissaoNegadaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.ReservaMapper;
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
        validarSalaAtiva(salaId);
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
    public Reserva cancelarReserva(UUID reservaId, UUID solicitanteUsuarioId, boolean solicitanteAdmin) {
        if (reservaId == null || solicitanteUsuarioId == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para cancelar reserva nao informados.");
        }
        ReservaEntity reservaEntity = buscarReservaEntity(reservaId);
        validarPermissaoCancelamento(reservaEntity, solicitanteUsuarioId, solicitanteAdmin);

        Reserva reserva = reservaMapper.toDomain(reservaEntity);
        Usuario solicitante = criarSolicitanteAutorizado(solicitanteUsuarioId, solicitanteAdmin);
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
    public Reserva alterarReserva(UUID reservaId, PeriodoReserva novoPeriodo, UUID solicitanteUsuarioId, boolean solicitanteAdmin) {
        if (reservaId == null || novoPeriodo == null || solicitanteUsuarioId == null) {
            throw new RegraDeNegocioException("Dados obrigatorios para alterar reserva nao informados.");
        }
        ReservaEntity reservaEntity = buscarReservaEntity(reservaId);
        validarPermissaoAlteracao(reservaEntity, solicitanteUsuarioId, solicitanteAdmin);
        validarSalaExistente(reservaEntity.getSalaId());
        validarSalaAtiva(reservaEntity.getSalaId());
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

    @Transactional(readOnly = true)
    public List<Reserva> listarReservas() {
        return reservaRepositorio.findAll().stream()
                .map(reservaMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Reserva obterReserva(UUID reservaId) {
        return reservaMapper.toDomain(buscarReservaEntity(reservaId));
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarReservasPorSala(UUID salaId) {
        return reservaRepositorio.findBySalaId(salaId).stream()
                .map(reservaMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarReservasPorUsuario(UUID usuarioId) {
        return reservaRepositorio.findByUsuarioId(usuarioId).stream()
                .map(reservaMapper::toDomain)
                .toList();
    }

    private void validarSalaExistente(UUID salaId) {
        if (!salaRepositorio.existsById(salaId)) {
            throw new EntidadeNaoEncontradaException("Sala nao encontrada.");
        }
    }

    private void validarPermissaoCancelamento(ReservaEntity reservaEntity,
                                              UUID solicitanteUsuarioId,
                                              boolean solicitanteAdmin) {
        if (!solicitanteAdmin && !reservaEntity.getUsuarioId().equals(solicitanteUsuarioId)) {
            throw new PermissaoNegadaException("Permissao negada para cancelar esta reserva.");
        }
    }

    private void validarPermissaoAlteracao(ReservaEntity reservaEntity,
                                           UUID solicitanteUsuarioId,
                                           boolean solicitanteAdmin) {
        if (!solicitanteAdmin && !reservaEntity.getUsuarioId().equals(solicitanteUsuarioId)) {
            throw new PermissaoNegadaException("Permissao negada para alterar esta reserva.");
        }
    }

    private Usuario criarSolicitanteAutorizado(UUID solicitanteUsuarioId, boolean solicitanteAdmin) {
        return new Usuario(
                solicitanteUsuarioId,
                "Solicitante",
                "solicitante@salalivre.local",
                solicitanteAdmin ? TipoUsuario.ADMIN : TipoUsuario.COMUM
        );
    }

    private void validarSalaAtiva(UUID salaId) {
        SalaEntity sala = salaRepositorio.findById(salaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala nao encontrada."));
        if (!sala.isAtiva()) {
            throw new RegraDeNegocioException("Sala inativa. Nao e possivel reservar.");
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

}
