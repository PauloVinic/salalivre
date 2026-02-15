package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void toEntity_e_toDomain_devemPreservarCampos() {
        Usuario domain = new Usuario(
                UUID.randomUUID(),
                "Usuario",
                "usuario@sala.com",
                TipoUsuario.COMUM
        );

        UsuarioEntity entity = mapper.toEntity(domain);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getNome(), entity.getNome());
        assertEquals(domain.getEmail(), entity.getEmail());
        assertEquals(domain.getTipo(), entity.getTipo());

        Usuario mappedBack = mapper.toDomain(entity);
        assertEquals(domain.getId(), mappedBack.getId());
        assertEquals(domain.getNome(), mappedBack.getNome());
        assertEquals(domain.getEmail(), mappedBack.getEmail());
        assertEquals(domain.getTipo(), mappedBack.getTipo());
    }

    @Test
    void atualizarEntity_deveAtualizarCamposEIgnorarQuandoNulos() {
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .nome("Antigo")
                .email("antigo@sala.com")
                .tipo(TipoUsuario.COMUM)
                .build();

        Usuario domain = new Usuario(
                entity.getId(),
                "Novo",
                "novo@sala.com",
                TipoUsuario.ADMIN
        );

        mapper.atualizarEntity(domain, entity);
        assertEquals("Novo", entity.getNome());
        assertEquals("novo@sala.com", entity.getEmail());
        assertEquals(TipoUsuario.ADMIN, entity.getTipo());

        mapper.atualizarEntity(null, entity);
        mapper.atualizarEntity(domain, null);
    }

    @Test
    void mapeamento_deveRetornarNuloQuandoEntradaNula() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null));
    }
}

