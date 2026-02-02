package br.com.fiap.salalivre.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.model.StatusReserva;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.SalaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;

@Service
public class DisponibilidadeAppService {
    private final SalaJpaRepository salaRepositorio;
    private final ReservaJpaRepository reservaRepositorio;
    private final SalaMapper salaMapper = new SalaMapper();

    public DisponibilidadeAppService(SalaJpaRepository salaRepositorio, ReservaJpaRepository reservaRepositorio) {
        this.salaRepositorio = salaRepositorio;
        this.reservaRepositorio = reservaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Sala> listarSalasDisponiveis(PeriodoReserva periodo) {
        if (periodo == null) {
            throw new RegraDeNegocioException("Periodo obrigatorio para consultar disponibilidade.");
        }
        List<Sala> disponiveis = new ArrayList<>();
        for (SalaEntity sala : salaRepositorio.findByAtivaTrue()) {
            boolean temConflito = !reservaRepositorio.findConflitos(
                    sala.getId(),
                    periodo.inicio(),
                    periodo.fim(),
                    StatusReserva.CANCELADA
            ).isEmpty();
            if (!temConflito) {
                disponiveis.add(salaMapper.toDomain(sala));
            }
        }
        return disponiveis;
    }
}
