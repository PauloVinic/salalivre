package br.com.fiap.salalivre.application.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.fiap.salalivre.domain.event.ReservaAlteradaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCanceladaEvent;
import br.com.fiap.salalivre.domain.event.ReservaCriadaEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {
    private static final Logger LOGGER = Logger.getLogger(NotificacaoService.class.getName());

    public void notificarReservaCriada(ReservaCriadaEvent evento) {
        logEvento("Reserva criada", evento);
    }

    public void notificarReservaCancelada(ReservaCanceladaEvent evento) {
        logEvento("Reserva cancelada", evento);
    }

    public void notificarReservaAlterada(ReservaAlteradaEvent evento) {
        logEvento("Reserva alterada", evento);
    }

    public void logEvento(Object evento) {
        logEvento("Notificacao", evento);
    }

    private void logEvento(String acao, Object evento) {
        if (evento == null) {
            LOGGER.log(Level.WARNING, "Evento de notificacao nao informado para acao: {0}", acao);
            return;
        }
        LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{acao, evento});
    }
}
