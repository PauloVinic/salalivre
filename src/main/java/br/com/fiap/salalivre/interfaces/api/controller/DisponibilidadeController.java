package br.com.fiap.salalivre.interfaces.api.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.salalivre.application.service.DisponibilidadeAppService;
import br.com.fiap.salalivre.domain.model.Sala;
import br.com.fiap.salalivre.domain.valueobject.PeriodoReserva;
import br.com.fiap.salalivre.interfaces.api.response.SalaResponse;

@RestController
@RequestMapping("/api/v1/disponibilidade")
@Validated
public class DisponibilidadeController {
    private final DisponibilidadeAppService disponibilidadeAppService;

    public DisponibilidadeController(DisponibilidadeAppService disponibilidadeAppService) {
        this.disponibilidadeAppService = disponibilidadeAppService;
    }

    @GetMapping
    public List<SalaResponse> listar(@RequestParam @NotNull @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime inicio,
                                     @RequestParam @NotNull @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime fim) {
        PeriodoReserva periodo = new PeriodoReserva(inicio, fim);
        return disponibilidadeAppService.listarSalasDisponiveis(periodo).stream()
                .map(this::toResponse)
                .toList();
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
