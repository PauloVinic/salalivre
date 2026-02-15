package br.com.fiap.salalivre.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UsuarioDomainTest {

    @Test
    void constructor_deveLancarParaCamposInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(null, "Usuario", "usuario@sala.com", TipoUsuario.COMUM));
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(UUID.randomUUID(), null, "usuario@sala.com", TipoUsuario.COMUM));
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(UUID.randomUUID(), " ", "usuario@sala.com", TipoUsuario.COMUM));
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(UUID.randomUUID(), "Usuario", null, TipoUsuario.COMUM));
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(UUID.randomUUID(), "Usuario", " ", TipoUsuario.COMUM));
        assertThrows(IllegalArgumentException.class,
                () -> new Usuario(UUID.randomUUID(), "Usuario", "usuario@sala.com", null));
    }

    @Test
    void alterarCampos_deveAtualizarQuandoValidos() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "Usuario", "usuario@sala.com", TipoUsuario.COMUM);

        usuario.alterarNome("Administrador");
        usuario.alterarEmail("admin@sala.com");
        usuario.alterarTipo(TipoUsuario.ADMIN);

        assertEquals("Administrador", usuario.getNome());
        assertEquals("admin@sala.com", usuario.getEmail());
        assertEquals(TipoUsuario.ADMIN, usuario.getTipo());
    }

    @Test
    void alterarCampos_deveLancarParaDadosInvalidos() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "Usuario", "usuario@sala.com", TipoUsuario.COMUM);

        assertThrows(IllegalArgumentException.class, () -> usuario.alterarNome(null));
        assertThrows(IllegalArgumentException.class, () -> usuario.alterarNome(" "));
        assertThrows(IllegalArgumentException.class, () -> usuario.alterarEmail(null));
        assertThrows(IllegalArgumentException.class, () -> usuario.alterarEmail(" "));
        assertThrows(IllegalArgumentException.class, () -> usuario.alterarTipo(null));
    }
}

