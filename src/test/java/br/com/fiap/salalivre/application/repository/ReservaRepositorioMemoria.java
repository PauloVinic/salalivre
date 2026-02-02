package br.com.fiap.salalivre.application.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import br.com.fiap.salalivre.domain.model.Reserva;

public class ReservaRepositorioMemoria {
    private final Map<UUID, Reserva> reservas = new ConcurrentHashMap<>();

    public void salvar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva obrigatoria.");
        }
        reservas.put(reserva.getId(), reserva);
    }

    public Reserva buscarPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id da reserva e obrigatorio.");
        }
        return reservas.get(id);
    }

    public List<Reserva> listarPorSalaId(UUID salaId) {
        if (salaId == null) {
            throw new IllegalArgumentException("Id da sala e obrigatorio.");
        }
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva reserva : reservas.values()) {
            if (salaId.equals(reserva.getSalaId())) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    public List<Reserva> listarTodas() {
        return new ArrayList<>(reservas.values());
    }
}
