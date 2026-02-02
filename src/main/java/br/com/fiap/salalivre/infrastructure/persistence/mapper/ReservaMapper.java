package br.com.fiap.salalivre.infrastructure.persistence.mapper;

import br.com.fiap.salalivre.domain.model.Reserva;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;

public class ReservaMapper {
    public Reserva toDomain(ReservaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Reserva(
                entity.getId(),
                entity.getSalaId(),
                entity.getUsuarioId(),
                new PeriodoReserva(entity.getInicio(), entity.getFim()),
                entity.getStatus(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }

    public ReservaEntity toEntity(Reserva domain) {
        if (domain == null) {
            return null;
        }
        return ReservaEntity.builder()
                .id(domain.getId())
                .salaId(domain.getSalaId())
                .usuarioId(domain.getUsuarioId())
                .inicio(domain.getPeriodo().inicio())
                .fim(domain.getPeriodo().fim())
                .status(domain.getStatus())
                .criadoEm(domain.getCriadoEm())
                .atualizadoEm(domain.getAtualizadoEm())
                .build();
    }

    public void atualizarEntity(Reserva domain, ReservaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        entity.setSalaId(domain.getSalaId());
        entity.setUsuarioId(domain.getUsuarioId());
        entity.setInicio(domain.getPeriodo().inicio());
        entity.setFim(domain.getPeriodo().fim());
        entity.setStatus(domain.getStatus());
        entity.setCriadoEm(domain.getCriadoEm());
        entity.setAtualizadoEm(domain.getAtualizadoEm());
    }
}
