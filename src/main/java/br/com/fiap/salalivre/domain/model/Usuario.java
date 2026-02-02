package br.com.fiap.salalivre.domain.model;

import java.util.UUID;

public class Usuario {
    private final UUID id;
    private String nome;
    private String email;
    private TipoUsuario tipo;

    public Usuario(UUID id, String nome, String email, TipoUsuario tipo) {
        if (id == null) {
            throw new IllegalArgumentException("Id do usuario e obrigatorio.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do usuario e obrigatorio.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do usuario e obrigatorio.");
        }
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuario e obrigatorio.");
        }
        this.nome = nome;
    }

    public void alterarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do usuario e obrigatorio.");
        }
        this.email = email;
    }

    public void alterarTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do usuario e obrigatorio.");
        }
        this.tipo = tipo;
    }
}
