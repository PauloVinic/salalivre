package br.com.fiap.salalivre.interfaces.api.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.salalivre.application.service.SalaAppService;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.interfaces.api.request.SalaCreateRequest;
import br.com.fiap.salalivre.interfaces.api.response.SalaResponse;

@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {
    private final SalaAppService salaAppService;

    public SalaController(SalaAppService salaAppService) {
        this.salaAppService = salaAppService;
    }

    @PostMapping
    public ResponseEntity<SalaResponse> cadastrar(@Valid @RequestBody SalaCreateRequest request) {
        Sala sala = salaAppService.cadastrarSala(
                request.nome(),
                request.capacidade(),
                request.localizacao(),
                request.recursos()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(sala));
    }

    @GetMapping
    public List<SalaResponse> listar() {
        return salaAppService.listarSalas().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public SalaResponse obter(@PathVariable UUID id) {
        return toResponse(salaAppService.obterSala(id));
    }

    @PatchMapping("/{id}/ativar")
    public SalaResponse ativar(@PathVariable UUID id) {
        return toResponse(salaAppService.ativarSala(id));
    }

    @PatchMapping("/{id}/desativar")
    public SalaResponse desativar(@PathVariable UUID id) {
        return toResponse(salaAppService.desativarSala(id));
    }

    private SalaResponse toResponse(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidade(),
                sala.getLocalizacao(),
                sala.getRecursos(),
                sala.isAtiva()
        );
    }
}
