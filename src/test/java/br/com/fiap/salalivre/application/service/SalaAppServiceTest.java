package br.com.fiap.salalivre.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;

@ExtendWith(MockitoExtension.class)
class SalaAppServiceTest {

    @Mock
    private SalaJpaRepository salaRepositorio;

    private SalaAppService salaAppService;

    @BeforeEach
    void setUp() {
        salaAppService = new SalaAppService(salaRepositorio);
    }

    @Test
    void cadastrarSala_deveLancarRegraDeNegocioQuandoDadosObrigatoriosAusentes() {
        assertThrows(RegraDeNegocioException.class,
                () -> salaAppService.cadastrarSala(" ", 10, "Andar 1", List.of("TV")));
        assertThrows(RegraDeNegocioException.class,
                () -> salaAppService.cadastrarSala("Sala Azul", 10, " ", List.of("TV")));
    }

    @Test
    void cadastrarSala_deveSalvarComRecursosVaziosQuandoNulo() {
        when(salaRepositorio.save(any(SalaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala sala = salaAppService.cadastrarSala("Sala Azul", 10, "Andar 1", null);

        ArgumentCaptor<SalaEntity> captor = ArgumentCaptor.forClass(SalaEntity.class);
        verify(salaRepositorio).save(captor.capture());

        SalaEntity salva = captor.getValue();
        assertEquals("Sala Azul", salva.getNome());
        assertEquals(10, salva.getCapacidade());
        assertEquals("Andar 1", salva.getLocalizacao());
        assertTrue(salva.getRecursos().isEmpty());
        assertTrue(salva.isAtiva());

        assertEquals("Sala Azul", sala.getNome());
        assertTrue(sala.getRecursos().isEmpty());
        assertTrue(sala.isAtiva());
    }

    @Test
    void listarSalas_deveMapearTodas() {
        SalaEntity salaA = salaEntity(UUID.randomUUID(), "Sala A", true);
        SalaEntity salaB = salaEntity(UUID.randomUUID(), "Sala B", false);
        when(salaRepositorio.findAll()).thenReturn(List.of(salaA, salaB));

        List<Sala> salas = salaAppService.listarSalas();

        assertEquals(2, salas.size());
        assertEquals("Sala A", salas.getFirst().getNome());
        assertEquals("Sala B", salas.get(1).getNome());
    }

    @Test
    void obterSala_deveLancarQuandoNaoEncontrada() {
        UUID salaId = UUID.randomUUID();
        when(salaRepositorio.findById(salaId)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> salaAppService.obterSala(salaId));
    }

    @Test
    void ativarSala_deveAtualizarStatusParaAtiva() {
        UUID salaId = UUID.randomUUID();
        SalaEntity entity = salaEntity(salaId, "Sala Inativa", false);
        when(salaRepositorio.findById(salaId)).thenReturn(Optional.of(entity));
        when(salaRepositorio.save(any(SalaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala ativada = salaAppService.ativarSala(salaId);

        assertTrue(ativada.isAtiva());
        verify(salaRepositorio).save(any(SalaEntity.class));
    }

    @Test
    void desativarSala_deveAtualizarStatusParaInativa() {
        UUID salaId = UUID.randomUUID();
        SalaEntity entity = salaEntity(salaId, "Sala Ativa", true);
        when(salaRepositorio.findById(salaId)).thenReturn(Optional.of(entity));
        when(salaRepositorio.save(any(SalaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sala desativada = salaAppService.desativarSala(salaId);

        assertTrue(!desativada.isAtiva());
        verify(salaRepositorio).save(any(SalaEntity.class));
    }

    @Test
    void listarSalasAtivas_deveMapearSomenteAtivas() {
        SalaEntity ativa = salaEntity(UUID.randomUUID(), "Sala Ativa", true);
        when(salaRepositorio.findByAtivaTrue()).thenReturn(List.of(ativa));

        List<Sala> salas = salaAppService.listarSalasAtivas();

        assertEquals(1, salas.size());
        assertEquals("Sala Ativa", salas.getFirst().getNome());
        assertTrue(salas.getFirst().isAtiva());
    }

    private SalaEntity salaEntity(UUID id, String nome, boolean ativa) {
        return SalaEntity.builder()
                .id(id)
                .nome(nome)
                .capacidade(10)
                .localizacao("Andar 1")
                .recursos(List.of("TV"))
                .ativa(ativa)
                .build();
    }
}

