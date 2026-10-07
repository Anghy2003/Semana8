package edu.uees.reservas.service;

import edu.uees.reservas.repository.ReservaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReservaServiceTest {

    private final ReservaRepository repository = mock(ReservaRepository.class);
    private final ReservaService service = new ReservaService(repository);

    // ------------------------------------------- regla de cancelacion (>= 2)

    @Test
    void cincoHorasPermitenCancelar() {
        // Act
        boolean resultado = service.puedeCancelar(5);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void dosHorasPermitenCancelar() {
        // Act
        boolean resultado = service.puedeCancelar(2);

        // Assert: limite incluido; detecta si alguien cambia >= por >
        assertTrue(resultado);
    }

    @Test
    void unaHoraNoPermiteCancelar() {
        // Act
        boolean resultado = service.puedeCancelar(1);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void ceroHorasNoPermiteCancelar() {
        // Act
        boolean resultado = service.puedeCancelar(0);

        // Assert
        assertFalse(resultado);
    }
}
