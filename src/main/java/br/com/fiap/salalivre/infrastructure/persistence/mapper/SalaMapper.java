package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import java.util.ArrayList;

import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;

public class SalaMapper {
    public Sala toDomain(SalaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Sala(
                entity.getId(),
                entity.getNome(),
                entity.getCapacidade(),
                entity.getLocalizacao(),
                entity.getRecursos() == null ? new ArrayList<>() : new ArrayList<>(entity.getRecursos()),
                entity.isAtiva()
        );
    }

    public SalaEntity toEntity(Sala domain) {
        if (domain == null) {
            return null;
        }
        return SalaEntity.builder()
                .id(domain.getId())
                .nome(domain.getNome())
                .capacidade(domain.getCapacidade())
                .localizacao(domain.getLocalizacao())
                .recursos(new ArrayList<>(domain.getRecursos()))
                .ativa(domain.isAtiva())
                .build();
    }

    public void atualizarEntity(Sala domain, SalaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        entity.setNome(domain.getNome());
        entity.setCapacidade(domain.getCapacidade());
        entity.setLocalizacao(domain.getLocalizacao());
        entity.setRecursos(new ArrayList<>(domain.getRecursos()));
        entity.setAtiva(domain.isAtiva());
    }
}
