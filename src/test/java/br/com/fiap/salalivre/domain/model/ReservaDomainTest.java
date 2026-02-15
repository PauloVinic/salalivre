package br.com.fiap.salalivre.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;

class ReservaDomainTest {

    @Test
    void cancelar_deveLancarQuandoJaCancelada() {
        UUID donoId = UUID.randomUUID();
        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                donoId,
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 21, 9, 0),
                        LocalDateTime.of(2026, 1, 21, 10, 0)
                ),
                StatusReserva.CANCELADA,
                LocalDateTime.of(2026, 1, 1, 8, 0),
                LocalDateTime.of(2026, 1, 1, 8, 0)
        );

        Usuario outroUsuario = new Usuario(
                UUID.randomUUID(),
                "Outro",
                "outro@sala.com",
                TipoUsuario.COMUM
        );

        assertThrows(RegraDeNegocioException.class, () -> reserva.cancelar(outroUsuario));
    }

    @Test
    void alterar_deveAtualizarStatusParaAlterada() {
        Reserva reserva = criarReservaConfirmada();
        LocalDateTime atualizadoAntes = reserva.getAtualizadoEm();

        PeriodoReserva novoPeriodo = new PeriodoReserva(
                LocalDateTime.of(2026, 1, 21, 11, 0),
                LocalDateTime.of(2026, 1, 21, 12, 0)
        );

        reserva.alterarPeriodo(novoPeriodo);

        assertEquals(StatusReserva.ALTERADA, reserva.getStatus());
        assertEquals(novoPeriodo, reserva.getPeriodo());
        assertTrue(!reserva.getAtualizadoEm().isBefore(atualizadoAntes));
    }

    @Test
    void alterar_deveLancarQuandoPeriodoInvalido() {
        Reserva reserva = criarReservaConfirmada();

        assertThrows(RegraDeNegocioException.class, () -> reserva.alterarPeriodo(
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 21, 12, 0),
                        LocalDateTime.of(2026, 1, 21, 12, 0)
                )
        ));
    }

    @Test
    void criarReservaPeriodoInvalido_deveLancarRegraDeNegocio() {
        assertThrows(RegraDeNegocioException.class, () -> new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 21, 14, 0),
                        LocalDateTime.of(2026, 1, 21, 13, 0)
                )
        ));
    }

    private Reserva criarReservaConfirmada() {
        return new Reserva(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 21, 9, 0),
                        LocalDateTime.of(2026, 1, 21, 10, 0)
                )
        );
    }
}
