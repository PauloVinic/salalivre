package br.com.fiap.salalivre.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;

public interface SalaJpaRepository extends JpaRepository<SalaEntity, UUID> {
    List<SalaEntity> findByAtivaTrue();
}
