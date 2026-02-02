package br.com.fiap.salalivre.interfaces.api.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ReservaAlterarRequest(
        @NotNull LocalDateTime inicio,
        @NotNull LocalDateTime fim
) {
}
