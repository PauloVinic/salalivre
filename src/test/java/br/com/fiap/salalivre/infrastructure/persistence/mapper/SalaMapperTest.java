package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;

class SalaMapperTest {

    private final SalaMapper mapper = new SalaMapper();

    @Test
    void toEntity_e_toDomain_devemPreservarCamposPrincipais() {
        Sala domain = new Sala(
                UUID.randomUUID(),
                "Sala Azul",
                10,
                "Andar 1",
                List.of("Projetor"),
                true
        );

        SalaEntity entity = mapper.toEntity(domain);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getNome(), entity.getNome());
        assertEquals(domain.getCapacidade(), entity.getCapacidade());
        assertEquals(domain.getLocalizacao(), entity.getLocalizacao());
        assertEquals(domain.getRecursos(), entity.getRecursos());
        assertTrue(entity.isAtiva());

        Sala mappedBack = mapper.toDomain(entity);
        assertEquals(domain.getId(), mappedBack.getId());
        assertEquals(domain.getNome(), mappedBack.getNome());
        assertEquals(domain.getCapacidade(), mappedBack.getCapacidade());
        assertEquals(domain.getLocalizacao(), mappedBack.getLocalizacao());
        assertEquals(domain.getRecursos(), mappedBack.getRecursos());
        assertTrue(mappedBack.isAtiva());
    }

    @Test
    void toDomain_deveUsarListaVaziaQuandoRecursosNulos() {
        SalaEntity entity = SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Sem Recursos")
                .capacidade(6)
                .localizacao("Andar 2")
                .recursos(null)
                .ativa(false)
                .build();

        Sala domain = mapper.toDomain(entity);

        assertTrue(domain.getRecursos().isEmpty());
        assertFalse(domain.isAtiva());
    }

    @Test
    void atualizarEntity_deveAtualizarCamposEIgnorarQuandoNulos() {
        SalaEntity entity = SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Antiga")
                .capacidade(6)
                .localizacao("Andar 1")
                .recursos(new ArrayList<>(List.of("TV")))
                .ativa(false)
                .build();

        Sala domain = new Sala(
                entity.getId(),
                "Sala Nova",
                12,
                "Andar 4",
                List.of("Projetor", "Webcam"),
                true
        );

        mapper.atualizarEntity(domain, entity);
        assertEquals("Sala Nova", entity.getNome());
        assertEquals(12, entity.getCapacidade());
        assertEquals("Andar 4", entity.getLocalizacao());
        assertEquals(List.of("Projetor", "Webcam"), entity.getRecursos());
        assertTrue(entity.isAtiva());

        mapper.atualizarEntity(null, entity);
        mapper.atualizarEntity(domain, null);
    }

    @Test
    void mapeamento_deveRetornarNuloQuandoEntradaNula() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null));
    }
}

