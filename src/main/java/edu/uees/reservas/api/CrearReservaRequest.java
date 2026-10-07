package edu.uees.reservas.api;

import jakarta.validation.constraints.NotBlank;

public record CrearReservaRequest(
        @NotBlank String id,
        @NotBlank String tipo
) {}
