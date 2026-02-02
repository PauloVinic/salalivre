package br.com.fiap.salalivre.infrastructure.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.domain.model.Usuario;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.ReservaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.SalaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.UsuarioMapper;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.UsuarioJpaRepository;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {
    private final SalaJpaRepository salaRepositorio;
    private final UsuarioJpaRepository usuarioRepositorio;
    private final ReservaJpaRepository reservaRepositorio;
    private final SalaMapper salaMapper = new SalaMapper();
    private final UsuarioMapper usuarioMapper = new UsuarioMapper();
    private final ReservaMapper reservaMapper = new ReservaMapper();

    public DataSeeder(SalaJpaRepository salaRepositorio,
                      UsuarioJpaRepository usuarioRepositorio,
                      ReservaJpaRepository reservaRepositorio) {
        this.salaRepositorio = salaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    @Override
    public void run(String... args) {
        if (salaRepositorio.count() > 0 || usuarioRepositorio.count() > 0 || reservaRepositorio.count() > 0) {
            return;
        }

        Usuario admin = new Usuario(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Administrador",
                "admin@salalivre.com",
                TipoUsuario.ADMIN
        );
        Usuario comum = new Usuario(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Usuario",
                "usuario@salalivre.com",
                TipoUsuario.COMUM
        );

        usuarioRepositorio.save(usuarioMapper.toEntity(admin));
        usuarioRepositorio.save(usuarioMapper.toEntity(comum));

        Sala salaAzul = new Sala(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Sala Azul",
                10,
                "Andar 1",
                List.of("Projetor", "Videoconferencia"),
                true
        );
        Sala salaVerde = new Sala(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Sala Verde",
                6,
                "Andar 2",
                List.of("TV"),
                true
        );

        salaRepositorio.save(salaMapper.toEntity(salaAzul));
        salaRepositorio.save(salaMapper.toEntity(salaVerde));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        PeriodoReserva periodo = new PeriodoReserva(inicio, inicio.plusHours(1));

        Reserva reserva = new Reserva(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                salaAzul.getId(),
                comum.getId(),
                periodo
        );

        reservaRepositorio.save(reservaMapper.toEntity(reserva));
    }
}
