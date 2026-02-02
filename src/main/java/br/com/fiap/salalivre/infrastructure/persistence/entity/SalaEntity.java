package br.com.fiap.salalivre.infrastructure.persistence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "salas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaEntity {
    @Id
    private UUID id;

    private String nome;

    private int capacidade;

    private String localizacao;

    @ElementCollection
    @CollectionTable(name = "sala_recursos", joinColumns = @JoinColumn(name = "sala_id"))
    @Column(name = "recurso")
    @Builder.Default
    private List<String> recursos = new ArrayList<>();

    private boolean ativa;
}
