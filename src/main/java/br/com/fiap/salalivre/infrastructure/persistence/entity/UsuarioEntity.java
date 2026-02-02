package br.com.fiap.salalivre.infrastructure.persistence.entity;

import java.util.UUID;

import br.com.fiap.salalivre.domain.model.TipoUsuario;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {
    @Id
    private UUID id;

    private String nome;

    private String email;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;
}
