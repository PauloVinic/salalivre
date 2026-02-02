package br.com.fiap.salalivre.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaCriadaEvent(
        UUID reservaId,
        UUID salaId,
        UUID usuarioId,
        LocalDateTime inicio,
        LocalDateTime fim
) {}
