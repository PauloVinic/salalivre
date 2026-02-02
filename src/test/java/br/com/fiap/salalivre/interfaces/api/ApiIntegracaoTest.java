package br.com.fiap.salalivre.interfaces.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
        SalaEntity sala = criarSalaPadrao("Sala Azul");
        UsuarioEntity usuario = criarUsuario("usuario@sala.com", TipoUsuario.COMUM);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);
        String payload = payloadReserva(sala.getId(), usuario.getId(), inicio, fim);

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void deveRetornarConflitoAoCriarReserva() throws Exception {
        SalaEntity sala = criarSalaPadrao("Sala Azul");
        UsuarioEntity usuario = criarUsuario("usuario@sala.com", TipoUsuario.COMUM);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        criarReservaPersistida(sala.getId(), usuario.getId(), inicio, fim, StatusReserva.CONFIRMADA);

        String payload = payloadReserva(
                sala.getId(),
                usuario.getId(),
                LocalDateTime.of(2026, 1, 20, 9, 30),
                LocalDateTime.of(2026, 1, 20, 10, 30)
        );

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void deveCriarReservaMesmoComCanceladaNoMesmoPeriodo() throws Exception {
        SalaEntity sala = criarSalaPadrao("Sala Azul");
        UsuarioEntity usuario = criarUsuario("usuario@sala.com", TipoUsuario.COMUM);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        criarReservaPersistida(sala.getId(), usuario.getId(), inicio, fim, StatusReserva.CANCELADA);

        String payload = payloadReserva(
                sala.getId(),
                usuario.getId(),
                LocalDateTime.of(2026, 1, 20, 9, 30),
                LocalDateTime.of(2026, 1, 20, 10, 30)
        );

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));
    }

    @Test
    void deveAlterarReservaMantendoPeriodoSemConflitoERecusarNovaReservaConflitante() throws Exception {
        SalaEntity sala = criarSalaPadrao("Sala Azul");
        UsuarioEntity usuario = criarUsuario("usuario@sala.com", TipoUsuario.COMUM);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        ReservaEntity reserva = criarReservaPersistida(
                sala.getId(),
                usuario.getId(),
                inicio,
                fim,
                StatusReserva.CONFIRMADA
        );

        String payloadAlterar = payloadAlterar(inicio, fim);

        mockMvc.perform(patch("/api/v1/reservas/{id}/alterar", reserva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadAlterar))
                .andExpect(status().isOk());

        String payloadConflito = payloadReserva(
                sala.getId(),
                usuario.getId(),
                LocalDateTime.of(2026, 1, 20, 9, 30),
                LocalDateTime.of(2026, 1, 20, 10, 30)
        );

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadConflito))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarSalasDisponiveis() throws Exception {
        SalaEntity salaOcupada = criarSala("Sala Ocupada", 10, "Andar 1", List.of("Projetor"));
        SalaEntity salaLivre = criarSala("Sala Livre", 6, "Andar 2", List.of("TV"));
        UsuarioEntity usuario = criarUsuario("usuario@sala.com", TipoUsuario.COMUM);

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 20, 10, 0);

        criarReservaPersistida(salaOcupada.getId(), usuario.getId(), inicio, fim, StatusReserva.CONFIRMADA);

        mockMvc.perform(get("/api/v1/disponibilidade")
                        .param("inicio", "2026-01-20T09:00:00")
                        .param("fim", "2026-01-20T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", hasItem(salaLivre.getId().toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(salaOcupada.getId().toString()))));
    }

    private SalaEntity criarSalaPadrao(String nome) {
        return criarSala(nome, 10, "Andar 1", List.of("Projetor"));
    }

    private SalaEntity criarSala(String nome, int capacidade, String localizacao, List<String> recursos) {
        return salaRepositorio.save(SalaEntity.builder()
                .id(UUID.randomUUID())
                .nome(nome)
                .capacidade(capacidade)
                .localizacao(localizacao)
                .recursos(recursos)
                .ativa(true)
                .build());
    }

    private UsuarioEntity criarUsuario(String email, TipoUsuario tipo) {
        return usuarioRepositorio.save(UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .nome("Usuario")
                .email(email)
                .tipo(tipo)
                .build());
    }

    private ReservaEntity criarReservaPersistida(UUID salaId,
                                                 UUID usuarioId,
                                                 LocalDateTime inicio,
                                                 LocalDateTime fim,
                                                 StatusReserva status) {
        return reservaRepositorio.save(ReservaEntity.builder()
                .id(UUID.randomUUID())
                .salaId(salaId)
                .usuarioId(usuarioId)
                .inicio(inicio)
                .fim(fim)
                .status(status)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build());
    }

    private String payloadReserva(UUID salaId, UUID usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return "{" +
                "\"salaId\":\"" + salaId + "\"," +
                "\"usuarioId\":\"" + usuarioId + "\"," +
                "\"inicio\":\"" + inicio + "\"," +
                "\"fim\":\"" + fim + "\"}";
    }

    private String payloadAlterar(LocalDateTime inicio, LocalDateTime fim) {
        return "{" +
                "\"inicio\":\"" + inicio + "\"," +
                "\"fim\":\"" + fim + "\"}";
    }

}
