package br.com.fiap.salalivre.domain.valueobject;

import java.time.LocalDateTime;

import br.com.fiap.salalivre.domain.exception.PeriodoInvalidoException;

public record PeriodoReserva(LocalDateTime inicio, LocalDateTime fim) {
    public PeriodoReserva {
        if (inicio == null || fim == null) {
            throw new PeriodoInvalidoException("Inicio e fim do periodo sao obrigatorios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new PeriodoInvalidoException("Fim do periodo deve ser posterior ao inicio.");
        }
    }
}
