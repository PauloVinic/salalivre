package br.com.fiap.salalivre.infrastructure.scheduler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.application.service.NotificacaoService;
import br.com.fiap.salalivre.domain.event.ReservaLembreteEvent;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;

@Component
public class LembreteReservaScheduler {
    private final ReservaJpaRepository reservaRepositorio;
    private final NotificacaoService notificacaoService;
    private final Clock clock;

    public LembreteReservaScheduler(ReservaJpaRepository reservaRepositorio,
                                    NotificacaoService notificacaoService,
                                    Clock clock) {
        this.reservaRepositorio = reservaRepositorio;
        this.notificacaoService = notificacaoService;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void enviarLembretesPendentes() {
        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime janelaInicio = agora.plusMinutes(10);
        LocalDateTime janelaFim = agora.plusMinutes(15);

        List<ReservaEntity> elegiveis = reservaRepositorio.findByStatusAndLembreteEnviadoFalseAndInicioBetween(
                StatusReserva.CONFIRMADA,
                janelaInicio,
                janelaFim
        );

        for (ReservaEntity reserva : elegiveis) {
            reserva.setLembreteEnviado(true);
            ReservaEntity salva = reservaRepositorio.save(reserva);
            notificacaoService.logEvento(new ReservaLembreteEvent(
                    salva.getId(),
                    salva.getSalaId(),
                    salva.getUsuarioId(),
                    salva.getInicio(),
                    salva.getFim()
            ));
        }
    }
}
