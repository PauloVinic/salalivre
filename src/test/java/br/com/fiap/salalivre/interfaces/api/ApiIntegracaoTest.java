package br.com.fiap.salalivre.interfaces.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.model.TipoUsuario;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.entity.UsuarioEntity;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.UsuarioJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
class ApiIntegracaoTest {

    private MockMvc mockMvc;


    @Autowired
    private SalaJpaRepository salaRepositorio;

    @Autowired
    private UsuarioJpaRepository usuarioRepositorio;

    @Autowired
    private ReservaJpaRepository reservaRepositorio;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        reservaRepositorio.deleteAll();
        salaRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void deveCadastrarSala() throws Exception {
        String payload = "{\"nome\":\"Sala Laranja\",\"capacidade\":8,\"localizacao\":\"Andar 3\",\"recursos\":[\"TV\"]}";

        mockMvc.perform(post("/api/v1/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void deveCriarReserva() throws Exception {
        SalaEntity sala = salaRepositorio.save(SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Azul")
                .capacidade(10)
                .localizacao("Andar 1")
                .recursos(List.of("Projetor"))
                .ativa(true)
                .build());

        UsuarioEntity usuario = usuarioRepositorio.save(UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .nome("Usuario")
                .email("usuario@sala.com")
                .tipo(TipoUsuario.COMUM)
                .build());

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);
        String payload = "{" +
                "\"salaId\":\"" + sala.getId() + "\"," +
                "\"usuarioId\":\"" + usuario.getId() + "\"," +
                "\"inicio\":\"" + inicio + "\"," +
                "\"fim\":\"" + fim + "\"}";

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void deveRetornarConflitoAoCriarReserva() throws Exception {
        SalaEntity sala = salaRepositorio.save(SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Azul")
                .capacidade(10)
                .localizacao("Andar 1")
                .recursos(List.of("Projetor"))
                .ativa(true)
                .build());

        UsuarioEntity usuario = usuarioRepositorio.save(UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .nome("Usuario")
                .email("usuario@sala.com")
                .tipo(TipoUsuario.COMUM)
                .build());

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        reservaRepositorio.save(ReservaEntity.builder()
                .id(UUID.randomUUID())
                .salaId(sala.getId())
                .usuarioId(usuario.getId())
                .inicio(inicio)
                .fim(fim)
                .status(StatusReserva.CONFIRMADA)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build());

        String payload = "{" +
                "\"salaId\":\"" + sala.getId() + "\"," +
                "\"usuarioId\":\"" + usuario.getId() + "\"," +
                "\"inicio\":\"" + LocalDateTime.of(2026, 1, 20, 9, 30) + "\"," +
                "\"fim\":\"" + LocalDateTime.of(2026, 1, 20, 10, 30) + "\"}";

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarSalasDisponiveis() throws Exception {
        SalaEntity salaOcupada = salaRepositorio.save(SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Ocupada")
                .capacidade(10)
                .localizacao("Andar 1")
                .recursos(List.of("Projetor"))
                .ativa(true)
                .build());

        SalaEntity salaLivre = salaRepositorio.save(SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome("Sala Livre")
                .capacidade(6)
                .localizacao("Andar 2")
                .recursos(List.of("TV"))
                .ativa(true)
                .build());

        UsuarioEntity usuario = usuarioRepositorio.save(UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .nome("Usuario")
                .email("usuario@sala.com")
                .tipo(TipoUsuario.COMUM)
                .build());

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        reservaRepositorio.save(ReservaEntity.builder()
                .id(UUID.randomUUID())
                .salaId(salaOcupada.getId())
                .usuarioId(usuario.getId())
                .inicio(inicio)
                .fim(fim)
                .status(StatusReserva.CONFIRMADA)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/disponibilidade")
                        .param("inicio", "2026-01-20T09:00:00")
                        .param("fim", "2026-01-20T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(salaLivre.getId().toString()));
    }

}
