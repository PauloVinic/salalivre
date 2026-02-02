package br.com.fiap.salalivre.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sala {
    private final UUID id;
    private String nome;
    private int capacidade;
    private String localizacao;
    private List<String> recursos;
    private boolean ativa;

    public Sala(UUID id, String nome, int capacidade, String localizacao, List<String> recursos, boolean ativa) {
        if (id == null) {
            throw new IllegalArgumentException("Id da sala e obrigatorio.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da sala e obrigatorio.");
        }
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade da sala deve ser maior que zero.");
        }
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localizacao da sala e obrigatoria.");
        }
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.recursos = recursos == null ? new ArrayList<>() : new ArrayList<>(recursos);
        this.ativa = ativa;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public List<String> getRecursos() {
        return new ArrayList<>(recursos);
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da sala e obrigatorio.");
        }
        this.nome = nome;
    }

    public void alterarCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade da sala deve ser maior que zero.");
        }
        this.capacidade = capacidade;
    }

    public void alterarLocalizacao(String localizacao) {
        if (localizacao == null || localizacao.isBlank()) {
            throw new IllegalArgumentException("Localizacao da sala e obrigatoria.");
        }
        this.localizacao = localizacao;
    }

    public void alterarRecursos(List<String> recursos) {
        this.recursos = recursos == null ? new ArrayList<>() : new ArrayList<>(recursos);
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }
}
