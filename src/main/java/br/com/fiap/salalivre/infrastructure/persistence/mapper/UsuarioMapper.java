package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;

public class UsuarioMapper {
    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Usuario(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTipo()
        );
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) {
            return null;
        }
        return UsuarioEntity.builder()
                .id(domain.getId())
                .nome(domain.getNome())
                .email(domain.getEmail())
                .tipo(domain.getTipo())
                .build();
    }

    public void atualizarEntity(Usuario domain, UsuarioEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        entity.setNome(domain.getNome());
        entity.setEmail(domain.getEmail());
        entity.setTipo(domain.getTipo());
    }
}
