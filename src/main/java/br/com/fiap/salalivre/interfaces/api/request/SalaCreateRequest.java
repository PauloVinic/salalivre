package br.com.fiap.salalivre.interfaces.api.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SalaCreateRequest(
        @NotBlank String nome,
        @Min(1) int capacidade,
        @NotBlank String localizacao,
        List<String> recursos
) {
}
