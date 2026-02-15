package br.com.fiap.salalivre.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SalaDomainTest {

    @Test
    void constructor_deveLancarParaCamposInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(null, "Sala", 10, "Andar 1", List.of("TV"), true));
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(UUID.randomUUID(), null, 10, "Andar 1", List.of("TV"), true));
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(UUID.randomUUID(), "   ", 10, "Andar 1", List.of("TV"), true));
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(UUID.randomUUID(), "Sala", 0, "Andar 1", List.of("TV"), true));
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(UUID.randomUUID(), "Sala", 10, null, List.of("TV"), true));
        assertThrows(IllegalArgumentException.class,
                () -> new Sala(UUID.randomUUID(), "Sala", 10, "   ", List.of("TV"), true));
    }

    @Test
    void constructor_deveCriarComListaVaziaQuandoRecursosNulos() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala", 8, "Andar 2", null, true);

        assertTrue(sala.getRecursos().isEmpty());
        assertTrue(sala.isAtiva());
    }

    @Test
    void alterarCampos_deveAtualizarQuandoValidos() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala Azul", 8, "Andar 1", List.of("TV"), true);

        sala.alterarNome("Sala Verde");
        sala.alterarCapacidade(12);
        sala.alterarLocalizacao("Andar 3");
        sala.alterarRecursos(List.of("Projetor", "TV"));
        sala.desativar();
        sala.ativar();

        assertEquals("Sala Verde", sala.getNome());
        assertEquals(12, sala.getCapacidade());
        assertEquals("Andar 3", sala.getLocalizacao());
        assertEquals(List.of("Projetor", "TV"), sala.getRecursos());
        assertTrue(sala.isAtiva());
    }

    @Test
    void alterarCampos_deveLancarParaDadosInvalidos() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala Azul", 8, "Andar 1", List.of("TV"), true);

        assertThrows(IllegalArgumentException.class, () -> sala.alterarNome(null));
        assertThrows(IllegalArgumentException.class, () -> sala.alterarNome(" "));
        assertThrows(IllegalArgumentException.class, () -> sala.alterarCapacidade(0));
        assertThrows(IllegalArgumentException.class, () -> sala.alterarLocalizacao(null));
        assertThrows(IllegalArgumentException.class, () -> sala.alterarLocalizacao(" "));
    }

    @Test
    void getRecursos_deveRetornarCopiaDefensiva() {
        Sala sala = new Sala(UUID.randomUUID(), "Sala Azul", 8, "Andar 1", List.of("TV"), true);

        List<String> recursosExpostos = sala.getRecursos();
        recursosExpostos.add("Projetor");

        assertEquals(List.of("TV"), sala.getRecursos());

        sala.alterarRecursos(null);
        assertTrue(sala.getRecursos().isEmpty());

        List<String> recursosMutaveis = new ArrayList<>(List.of("Notebook"));
        sala.alterarRecursos(recursosMutaveis);
        recursosMutaveis.add("Camera");

        assertFalse(sala.getRecursos().contains("Camera"));
    }
}

