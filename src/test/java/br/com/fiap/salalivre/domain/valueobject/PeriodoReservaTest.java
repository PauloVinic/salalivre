package br.com.fiap.salalivre.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.domain.exception.PeriodoInvalidoException;

class PeriodoReservaTest {

    @Test
    void constructor_deveLancarQuandoFimMenorOuIgualInicio() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 22, 9, 0);

        assertThrows(PeriodoInvalidoException.class,
                () -> new PeriodoReserva(inicio, inicio));
        assertThrows(PeriodoInvalidoException.class,
                () -> new PeriodoReserva(inicio, inicio.minusMinutes(1)));
    }

    @Test
    void podeCriarPeriodoValido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 22, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 22, 10, 0);

        PeriodoReserva periodo = new PeriodoReserva(inicio, fim);

        assertEquals(inicio, periodo.inicio());
        assertEquals(fim, periodo.fim());
    }
}

