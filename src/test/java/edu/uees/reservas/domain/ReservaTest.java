package edu.uees.reservas.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Agregada en la auditoria (Actividad 3): JaCoCo mostraba cancelar() sin
 * ejecutar y dos ramas a medias en el constructor (id nulo y tipo nulo).
 */
class ReservaTest {

    @Test
    void idNuloEsInvalido() {
        // Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> new Reserva(null, "NORMAL"));

        // Assert
        assertEquals("Id obligatorio", ex.getMessage());
    }

    @Test
    void tipoNuloSeAsumeNormal() {
        // Act
        Reserva reserva = new Reserva("R-001", null);

        // Assert
        assertEquals("NORMAL", reserva.getTipo());
    }

    @Test
    void cancelarCambiaElEstadoACancelada() {
        // Arrange
        Reserva reserva = new Reserva("R-002", "NORMAL");

        // Act
        reserva.cancelar();

        // Assert
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }
}
