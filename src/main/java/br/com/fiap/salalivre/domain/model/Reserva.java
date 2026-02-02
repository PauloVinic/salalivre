package br.com.fiap.salalivre.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.fiap.salalivre.domain.exception.PermissaoNegadaException;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;

public class Reserva {
    private final UUID id;
    private final UUID salaId;
    private final UUID usuarioId;
    private PeriodoReserva periodo;
    private StatusReserva status;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Reserva(UUID id, UUID salaId, UUID usuarioId, PeriodoReserva periodo) {
        if (id == null) {
            throw new IllegalArgumentException("Id da reserva e obrigatorio.");
        }
        if (salaId == null) {
            throw new IllegalArgumentException("Id da sala e obrigatorio.");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("Id do usuario e obrigatorio.");
        }
        if (periodo == null) {
            throw new IllegalArgumentException("Periodo da reserva e obrigatorio.");
        }
        this.id = id;
        this.salaId = salaId;
        this.usuarioId = usuarioId;
        this.periodo = periodo;
        this.status = StatusReserva.CONFIRMADA;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    public Reserva(UUID id, UUID salaId, UUID usuarioId, PeriodoReserva periodo, StatusReserva status,
                   LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        if (id == null) {
            throw new IllegalArgumentException("Id da reserva e obrigatorio.");
        }
        if (salaId == null) {
            throw new IllegalArgumentException("Id da sala e obrigatorio.");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("Id do usuario e obrigatorio.");
        }
        if (periodo == null) {
            throw new IllegalArgumentException("Periodo da reserva e obrigatorio.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status da reserva e obrigatorio.");
        }
        if (criadoEm == null || atualizadoEm == null) {
            throw new IllegalArgumentException("Datas de criacao/atualizacao sao obrigatorias.");
        }
        this.id = id;
        this.salaId = salaId;
        this.usuarioId = usuarioId;
        this.periodo = periodo;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSalaId() {
        return salaId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public PeriodoReserva getPeriodo() {
        return periodo;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void cancelar(Usuario solicitante) {
        if (solicitante == null) {
            throw new PermissaoNegadaException("Solicitante nao informado.");
        }
        boolean ehAdmin = solicitante.getTipo() == TipoUsuario.ADMIN;
        boolean ehDonoDaReserva = solicitante.getId().equals(this.usuarioId);
        if (!ehAdmin && !ehDonoDaReserva) {
            throw new PermissaoNegadaException("Solicitante nao possui permissao para cancelar a reserva.");
        }
        this.status = StatusReserva.CANCELADA;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void alterarPeriodo(PeriodoReserva novoPeriodo) {
        if (novoPeriodo == null) {
            throw new IllegalArgumentException("Novo periodo e obrigatorio.");
        }
        this.periodo = novoPeriodo;
        this.status = StatusReserva.ALTERADA;
        this.atualizadoEm = LocalDateTime.now();
    }
}
