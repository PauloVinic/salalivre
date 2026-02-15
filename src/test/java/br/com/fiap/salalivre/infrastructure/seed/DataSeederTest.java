package br.com.fiap.salalivre.infrastructure.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.UsuarioJpaRepository;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private SalaJpaRepository salaRepositorio;

    @Mock
    private UsuarioJpaRepository usuarioRepositorio;

    @Mock
    private ReservaJpaRepository reservaRepositorio;

    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        dataSeeder = new DataSeeder(salaRepositorio, usuarioRepositorio, reservaRepositorio);
    }

    @Test
    void run_deveRetornarQuandoJaHaSalas() throws Exception {
        when(salaRepositorio.count()).thenReturn(1L);

        dataSeeder.run();

        verify(usuarioRepositorio, never()).count();
        verify(reservaRepositorio, never()).count();
        verify(usuarioRepositorio, never()).save(any(UsuarioEntity.class));
        verify(salaRepositorio, never()).save(any(SalaEntity.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
    }

    @Test
    void run_deveRetornarQuandoJaHaUsuarios() throws Exception {
        when(salaRepositorio.count()).thenReturn(0L);
        when(usuarioRepositorio.count()).thenReturn(1L);

        dataSeeder.run();

        verify(reservaRepositorio, never()).count();
        verify(usuarioRepositorio, never()).save(any(UsuarioEntity.class));
        verify(salaRepositorio, never()).save(any(SalaEntity.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
    }

    @Test
    void run_deveRetornarQuandoJaHaReservas() throws Exception {
        when(salaRepositorio.count()).thenReturn(0L);
        when(usuarioRepositorio.count()).thenReturn(0L);
        when(reservaRepositorio.count()).thenReturn(1L);

        dataSeeder.run();

        verify(usuarioRepositorio, never()).save(any(UsuarioEntity.class));
        verify(salaRepositorio, never()).save(any(SalaEntity.class));
        verify(reservaRepositorio, never()).save(any(ReservaEntity.class));
    }

    @Test
    void run_devePopularQuandoRepositoriosVazios() throws Exception {
        when(salaRepositorio.count()).thenReturn(0L);
        when(usuarioRepositorio.count()).thenReturn(0L);
        when(reservaRepositorio.count()).thenReturn(0L);
        when(usuarioRepositorio.save(any(UsuarioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salaRepositorio.save(any(SalaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservaRepositorio.save(any(ReservaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataSeeder.run();

        ArgumentCaptor<UsuarioEntity> usuarioCaptor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepositorio, times(2)).save(usuarioCaptor.capture());
        List<UsuarioEntity> usuarios = usuarioCaptor.getAllValues();
        assertEquals(2, usuarios.size());
        assertEquals(TipoUsuario.ADMIN, usuarios.getFirst().getTipo());
        assertEquals(TipoUsuario.COMUM, usuarios.get(1).getTipo());

        ArgumentCaptor<SalaEntity> salaCaptor = ArgumentCaptor.forClass(SalaEntity.class);
        verify(salaRepositorio, times(2)).save(salaCaptor.capture());
        List<SalaEntity> salas = salaCaptor.getAllValues();
        assertEquals(2, salas.size());
        assertEquals("Sala Azul", salas.getFirst().getNome());
        assertEquals("Sala Verde", salas.get(1).getNome());

        ArgumentCaptor<ReservaEntity> reservaCaptor = ArgumentCaptor.forClass(ReservaEntity.class);
        verify(reservaRepositorio).save(reservaCaptor.capture());
        ReservaEntity reserva = reservaCaptor.getValue();
        assertEquals(UUID.fromString("33333333-3333-3333-3333-333333333333"), reserva.getSalaId());
        assertEquals(UUID.fromString("22222222-2222-2222-2222-222222222222"), reserva.getUsuarioId());
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
    }
}

