package br.com.fiap.salalivre.interfaces.api.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReservaCreateRequest(
        @NotNull UUID salaId,
        @NotNull UUID usuarioId,
        @NotNull LocalDateTime inicio,
        @NotNull LocalDateTime fim
) {
}
