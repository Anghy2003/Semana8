package edu.uees.reservas.api;

import jakarta.validation.constraints.NotBlank;

public record CrearReservaRequest(
        @NotBlank(message = "El id es obligatorio") String id,
        @NotBlank(message = "El tipo es obligatorio") String tipo
) {}
