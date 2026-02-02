package br.com.fiap.salalivre.interfaces.api.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReservaCancelarRequest(
        @NotNull UUID solicitanteUsuarioId
) {
}
