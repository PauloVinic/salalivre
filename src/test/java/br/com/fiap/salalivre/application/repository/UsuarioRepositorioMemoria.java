package br.com.fiap.salalivre.application.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import br.com.fiap.salalivre.domain.model.Usuario;

public class UsuarioRepositorioMemoria {
    private final Map<UUID, Usuario> usuarios = new ConcurrentHashMap<>();

    public void salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario obrigatorio.");
        }
        usuarios.put(usuario.getId(), usuario);
    }

    public Usuario buscarPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id do usuario e obrigatorio.");
        }
        return usuarios.get(id);
    }

    public boolean existe(UUID id) {
        if (id == null) {
            return false;
        }
        return usuarios.containsKey(id);
    }

    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }
}
