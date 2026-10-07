package edu.uees.reservas.service;

import edu.uees.reservas.domain.EstadoReserva;
import edu.uees.reservas.domain.Reserva;
import edu.uees.reservas.repository.ReservaRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // ------------------------------------------- crear, buscar y confirmar

    @Test
    void crearGuardaUnaReservaPendienteConLosDatosRecibidos() {
        // Arrange: el repositorio devuelve lo que recibe
        when(repository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reserva reserva = service.crear("R-001", "VIP");

        // Assert
        assertEquals("R-001", reserva.getId());
        assertEquals("VIP", reserva.getTipo());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        verify(repository).guardar(reserva);
    }

    @Test
    void crearConIdVacioNoLlegaAlRepositorio() {
        // Act & Assert: la regla vive en el dominio (Reserva)
        assertThrows(IllegalArgumentException.class, () -> service.crear(" ", "NORMAL"));
        verify(repository, never()).guardar(any());
    }

    @Test
    void crearConUnIdQueYaExisteLanzaDuplicadaYNoGuarda() {
        // Arrange
        when(repository.buscarPorId("R-001"))
                .thenReturn(Optional.of(new Reserva("R-001", "NORMAL")));

        // Act
        ReservaDuplicadaException ex = assertThrows(
                ReservaDuplicadaException.class, () -> service.crear("R-001", "VIP"));

        // Assert
        assertEquals("R-001", ex.getId());
        verify(repository, never()).guardar(any());
    }

    @Test
    void buscarDevuelveLaReservaExistente() {
        // Arrange
        Reserva guardada = new Reserva("R-002", "NORMAL");
        when(repository.buscarPorId("R-002")).thenReturn(Optional.of(guardada));

        // Act
        Reserva encontrada = service.buscar("R-002");

        // Assert
        assertSame(guardada, encontrada);
    }

    @Test
    void buscarUnIdInexistenteLanzaExcepcion() {
        // Arrange
        when(repository.buscarPorId("R-999")).thenReturn(Optional.empty());

        // Act
        ReservaNoEncontradaException ex = assertThrows(
                ReservaNoEncontradaException.class, () -> service.buscar("R-999"));

        // Assert: excepcion propia con el id que se busco
        assertEquals("Reserva no encontrada", ex.getMessage());
        assertEquals("R-999", ex.getId());
    }

    @Test
    void confirmarCambiaElEstadoYVuelveAGuardar() {
        // Arrange
        Reserva guardada = new Reserva("R-003", "NORMAL");
        when(repository.buscarPorId("R-003")).thenReturn(Optional.of(guardada));
        when(repository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reserva confirmada = service.confirmar("R-003");

        // Assert
        assertEquals(EstadoReserva.CONFIRMADA, confirmada.getEstado());
        verify(repository).guardar(guardada);
    }
}
