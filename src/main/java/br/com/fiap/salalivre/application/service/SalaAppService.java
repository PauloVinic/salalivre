package br.com.fiap.salalivre.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.salalivre.domain.exception.EntidadeNaoEncontradaException;
import br.com.fiap.salalivre.domain.exception.RegraDeNegocioException;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.infrastructure.persistence.entity.SalaEntity;
import br.com.fiap.salalivre.infrastructure.persistence.mapper.SalaMapper;
import br.com.fiap.salalivre.infrastructure.persistence.repository.SalaJpaRepository;

@Service
public class SalaAppService {
    private final SalaJpaRepository salaRepositorio;
    private final SalaMapper salaMapper = new SalaMapper();

    public SalaAppService(SalaJpaRepository salaRepositorio) {
        this.salaRepositorio = salaRepositorio;
    }

    @Transactional
    public Sala cadastrarSala(String nome, int capacidade, String localizacao, List<String> recursos) {
        if (nome == null || nome.isBlank() || localizacao == null || localizacao.isBlank()) {
            throw new RegraDeNegocioException("Dados obrigatorios para cadastrar sala nao informados.");
        }
        Sala sala = new Sala(UUID.randomUUID(), nome, capacidade, localizacao,
                recursos == null ? new ArrayList<>() : recursos, true);
        SalaEntity salva = salaRepositorio.save(salaMapper.toEntity(sala));
        return salaMapper.toDomain(salva);
    }

    @Transactional(readOnly = true)
    public List<Sala> listarSalas() {
        return salaRepositorio.findAll().stream()
                .map(salaMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Sala obterSala(UUID id) {
        SalaEntity entity = salaRepositorio.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala nao encontrada."));
        return salaMapper.toDomain(entity);
    }

    @Transactional
    public Sala ativarSala(UUID id) {
        SalaEntity entity = salaRepositorio.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala nao encontrada."));
        entity.setAtiva(true);
        return salaMapper.toDomain(salaRepositorio.save(entity));
    }

    @Transactional
    public Sala desativarSala(UUID id) {
        SalaEntity entity = salaRepositorio.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala nao encontrada."));
        entity.setAtiva(false);
        return salaMapper.toDomain(salaRepositorio.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Sala> listarSalasAtivas() {
        return salaRepositorio.findByAtivaTrue().stream()
                .map(salaMapper::toDomain)
                .toList();
    }
}
