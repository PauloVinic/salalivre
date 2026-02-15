package br.com.fiap.salalivre.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.salalivre.application.service.NotificacaoService;
import br.com.fiap.salalivre.domain.event.ReservaLembreteEvent;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;

@ExtendWith(MockitoExtension.class)
class LembreteReservaSchedulerTest {

    @Mock
    private ReservaJpaRepository reservaRepositorio;

    @Mock
    private NotificacaoService notificacaoService;

    private Clock clock;
    private LembreteReservaScheduler scheduler;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-01-10T12:00:00Z"), ZoneOffset.UTC);
        scheduler = new LembreteReservaScheduler(reservaRepositorio, notificacaoService, clock);
    }

    @Test
    void enviarLembretesPendentes_deveMarcarEnviarENotificarQuandoReservaNaJanela() {
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime janelaInicio = agora.plusMinutes(10);
        LocalDateTime janelaFim = agora.plusMinutes(15);

        UUID reservaId = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        ReservaEntity reserva = ReservaEntity.builder()
                .id(reservaId)
                .salaId(salaId)
                .usuarioId(usuarioId)
                .inicio(agora.plusMinutes(12))
                .fim(agora.plusMinutes(42))
                .status(StatusReserva.CONFIRMADA)
                .lembreteEnviado(false)
                .criadoEm(agora.minusDays(1))
                .atualizadoEm(agora.minusDays(1))
                .build();

        when(reservaRepositorio.findByStatusAndLembreteEnviadoFalseAndInicioBetween(
                StatusReserva.CONFIRMADA,
                janelaInicio,
                janelaFim
        )).thenReturn(List.of(reserva));
        when(reservaRepositorio.save(any(ReservaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.enviarLembretesPendentes();

        verify(reservaRepositorio).findByStatusAndLembreteEnviadoFalseAndInicioBetween(
                StatusReserva.CONFIRMADA,
                janelaInicio,
                janelaFim
        );

        ArgumentCaptor<ReservaEntity> entidadeCaptor = ArgumentCaptor.forClass(ReservaEntity.class);
        verify(reservaRepositorio).save(entidadeCaptor.capture());
        assertTrue(entidadeCaptor.getValue().isLembreteEnviado());

        ArgumentCaptor<Object> eventoCaptor = ArgumentCaptor.forClass(Object.class);
        verify(notificacaoService).logEvento(eventoCaptor.capture());
        ReservaLembreteEvent evento = assertInstanceOf(ReservaLembreteEvent.class, eventoCaptor.getValue());
        assertEquals(reservaId, evento.reservaId());
        assertEquals(salaId, evento.salaId());
        assertEquals(usuarioId, evento.usuarioId());
    }
}
