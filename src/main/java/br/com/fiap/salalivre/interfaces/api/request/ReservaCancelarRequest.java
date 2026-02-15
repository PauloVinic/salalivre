package br.com.fiap.salalivre.interfaces.api.request;

import java.util.UUID;

public record ReservaCancelarRequest(
        UUID solicitanteUsuarioId
) {
}
