package br.com.fiap.salalivre.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.fiap.salalivre.domain.model.StatusReserva;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaEntity {
    @Id
    private UUID id;

    private UUID salaId;

    private UUID usuarioId;

    private LocalDateTime inicio;

    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    private boolean lembreteEnviado;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;
}
