package br.com.fiap.salalivre.interfaces.api.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.salalivre.application.service.ReservaAppService;
import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.interfaces.api.request.ReservaAlterarRequest;
import br.com.fiap.salalivre.interfaces.api.request.ReservaCancelarRequest;
import br.com.fiap.salalivre.interfaces.api.request.ReservaCreateRequest;
import br.com.fiap.salalivre.interfaces.api.response.ReservaResponse;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {
    private final ReservaAppService reservaAppService;

    public ReservaController(ReservaAppService reservaAppService) {
        this.reservaAppService = reservaAppService;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(@Valid @RequestBody ReservaCreateRequest request) {
        PeriodoReserva periodo = new PeriodoReserva(request.inicio(), request.fim());
        Reserva reserva = reservaAppService.criarReserva(request.usuarioId(), request.salaId(), periodo);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reserva));
    }

    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable UUID id,
                                    @RequestHeader("X-User-Id") UUID solicitanteUsuarioId,
                                    @RequestHeader(value = "X-User-Role", required = false) String solicitanteRole,
                                    @RequestHeader(value = "X-Admin", required = false, defaultValue = "false") boolean adminHeader,
                                    @RequestBody(required = false) ReservaCancelarRequest request) {
        boolean solicitanteAdmin = ehAdmin(solicitanteRole, adminHeader);
        Reserva reserva = reservaAppService.cancelarReserva(id, solicitanteUsuarioId, solicitanteAdmin);
        return toResponse(reserva);
    }

    @PatchMapping("/{id}/alterar")
    public ReservaResponse alterar(@PathVariable UUID id,
                                   @RequestHeader("X-User-Id") UUID solicitanteUsuarioId,
                                   @RequestHeader(value = "X-User-Role", required = false) String solicitanteRole,
                                   @RequestHeader(value = "X-Admin", required = false, defaultValue = "false") boolean adminHeader,
                                   @Valid @RequestBody ReservaAlterarRequest request) {
        boolean solicitanteAdmin = ehAdmin(solicitanteRole, adminHeader);
        PeriodoReserva periodo = new PeriodoReserva(request.inicio(), request.fim());
        Reserva reserva = reservaAppService.alterarReserva(id, periodo, solicitanteUsuarioId, solicitanteAdmin);
        return toResponse(reserva);
    }

    private boolean ehAdmin(String roleHeader, boolean adminHeader) {
        return adminHeader || "ADMIN".equalsIgnoreCase(roleHeader);
    }

    private ReservaResponse toResponse(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getSalaId(),
                reserva.getUsuarioId(),
                reserva.getPeriodo().inicio(),
                reserva.getPeriodo().fim(),
                reserva.getStatus(),
                reserva.getCriadoEm(),
                reserva.getAtualizadoEm()
        );
    }
}
