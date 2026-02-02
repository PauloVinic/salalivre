package br.com.fiap.salalivre.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;

public interface SalaJpaRepository extends JpaRepository<SalaEntity, UUID> {
    List<SalaEntity> findByAtivaTrue();

    @Query("""
            select s from SalaEntity s
            where s.ativa = true
              and not exists (
                select r.id from ReservaEntity r
                where r.salaId = s.id
                  and r.status <> br.com.fiap.salalivre.domain.model.StatusReserva.CANCELADA
                  and :inicio < r.fim
                  and :fim > r.inicio
              )
            """)
    List<SalaEntity> findDisponiveis(@Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);
}
