package br.com.fiap.salalivre.application.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import br.com.fiap.salalivre.domain.model.Sala;

public class SalaRepositorioMemoria {
    private final Map<UUID, Sala> salas = new ConcurrentHashMap<>();

    public void salvar(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("Sala obrigatoria.");
        }
        salas.put(sala.getId(), sala);
    }

    public Sala buscarPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id da sala e obrigatorio.");
        }
        return salas.get(id);
    }

    public boolean existe(UUID id) {
        if (id == null) {
            return false;
        }
        return salas.containsKey(id);
    }

    public List<Sala> listarTodas() {
        return new ArrayList<>(salas.values());
    }
}
