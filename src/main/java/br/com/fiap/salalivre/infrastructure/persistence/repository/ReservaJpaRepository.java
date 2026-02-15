package br.com.fiap.salalivre.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.ReservaEntity;

public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, UUID> {

    @Query("""
            select r from ReservaEntity r
            where r.salaId = :salaId
              and r.status <> :statusCancelada
              and :inicio < r.fim
              and :fim > r.inicio
            """)
    List<ReservaEntity> findConflitos(@Param("salaId") UUID salaId,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim,
                                     @Param("statusCancelada") StatusReserva statusCancelada);

    @Query("""
            select r from ReservaEntity r
            where r.salaId = :salaId
              and r.status <> :statusCancelada
              and :inicio < r.fim
              and :fim > r.inicio
              and r.id <> :reservaId
            """)
    List<ReservaEntity> findConflitosExcluindoReserva(@Param("salaId") UUID salaId,
                                                      @Param("inicio") LocalDateTime inicio,
                                                      @Param("fim") LocalDateTime fim,
                                                      @Param("statusCancelada") StatusReserva statusCancelada,
                                                      @Param("reservaId") UUID reservaId);

    List<ReservaEntity> findBySalaId(UUID salaId);

    List<ReservaEntity> findByStatusAndLembreteEnviadoFalseAndInicioBetween(StatusReserva status,
                                                                             LocalDateTime inicio,
                                                                             LocalDateTime fim);
}
