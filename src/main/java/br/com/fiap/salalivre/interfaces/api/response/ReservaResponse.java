package br.com.fiap.salalivre.interfaces.api.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.fiap.salalivre.domain.model.StatusReserva;

public record ReservaResponse(
        UUID id,
        UUID salaId,
        UUID usuarioId,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusReserva status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
