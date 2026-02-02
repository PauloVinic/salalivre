package br.com.fiap.salalivre.interfaces.api.response;

import java.util.List;
import java.util.UUID;

public record SalaResponse(
        UUID id,
        String nome,
        int capacidade,
        String localizacao,
        List<String> recursos,
        boolean ativa
) {
}
