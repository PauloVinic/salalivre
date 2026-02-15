package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;

class ReservaMapperTest {

    private final ReservaMapper mapper = new ReservaMapper();

    @Test
    void toEntity_e_toDomain_preservamCamposImportantes() {
        UUID id = UUID.randomUUID();
        UUID salaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        PeriodoReserva periodo = new PeriodoReserva(
                LocalDateTime.of(2026, 1, 23, 9, 0),
                LocalDateTime.of(2026, 1, 23, 10, 0)
        );
        LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime atualizadoEm = LocalDateTime.of(2026, 1, 1, 9, 0);
        Reserva domain = new Reserva(id, salaId, usuarioId, periodo, StatusReserva.ALTERADA, criadoEm, atualizadoEm);

        ReservaEntity entity = mapper.toEntity(domain);

        assertEquals(id, entity.getId());
        assertEquals(salaId, entity.getSalaId());
        assertEquals(usuarioId, entity.getUsuarioId());
        assertEquals(StatusReserva.ALTERADA, entity.getStatus());
        assertEquals(periodo.inicio(), entity.getInicio());
        assertEquals(periodo.fim(), entity.getFim());
        assertFalse(entity.isLembreteEnviado());

        entity.setLembreteEnviado(true);

        Reserva mappedBack = mapper.toDomain(entity);
        assertEquals(id, mappedBack.getId());
        assertEquals(salaId, mappedBack.getSalaId());
        assertEquals(usuarioId, mappedBack.getUsuarioId());
        assertEquals(StatusReserva.ALTERADA, mappedBack.getStatus());
        assertEquals(periodo.inicio(), mappedBack.getPeriodo().inicio());
        assertEquals(periodo.fim(), mappedBack.getPeriodo().fim());
        assertEquals(criadoEm, mappedBack.getCriadoEm());
        assertEquals(atualizadoEm, mappedBack.getAtualizadoEm());
    }

    @Test
    void atualizarEntity_deveManterLembreteEnviadoEAplicarStatusEPeriodo() {
        ReservaEntity entity = ReservaEntity.builder()
                .id(UUID.randomUUID())
                .salaId(UUID.randomUUID())
                .usuarioId(UUID.randomUUID())
                .inicio(LocalDateTime.of(2026, 1, 23, 9, 0))
                .fim(LocalDateTime.of(2026, 1, 23, 10, 0))
                .status(StatusReserva.CONFIRMADA)
                .lembreteEnviado(true)
                .criadoEm(LocalDateTime.of(2026, 1, 1, 8, 0))
                .atualizadoEm(LocalDateTime.of(2026, 1, 1, 8, 0))
                .build();

        Reserva domain = new Reserva(
                entity.getId(),
                entity.getSalaId(),
                entity.getUsuarioId(),
                new PeriodoReserva(
                        LocalDateTime.of(2026, 1, 23, 11, 0),
                        LocalDateTime.of(2026, 1, 23, 12, 0)
                ),
                StatusReserva.ALTERADA,
                entity.getCriadoEm(),
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );

        mapper.atualizarEntity(domain, entity);

        assertEquals(StatusReserva.ALTERADA, entity.getStatus());
        assertEquals(domain.getPeriodo().inicio(), entity.getInicio());
        assertEquals(domain.getPeriodo().fim(), entity.getFim());
        assertTrue(entity.isLembreteEnviado());
    }

    @Test
    void mapeamento_deveRetornarNuloQuandoEntradaNula() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toEntity(null));
    }
}

