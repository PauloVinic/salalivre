package br.com.fiap.salalivre.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.SalaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;

@Service
public class DisponibilidadeAppService {
    private final SalaJpaRepository salaRepositorio;
    private final SalaMapper salaMapper = new SalaMapper();

    public DisponibilidadeAppService(SalaJpaRepository salaRepositorio) {
        this.salaRepositorio = salaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Sala> listarSalasDisponiveis(PeriodoReserva periodo) {
        if (periodo == null) {
            throw new RegraDeNegocioException("Periodo obrigatorio para consultar disponibilidade.");
        }
        return salaRepositorio.findDisponiveis(periodo.inicio(), periodo.fim()).stream()
                .map(salaMapper::toDomain)
                .toList();
    }
}
