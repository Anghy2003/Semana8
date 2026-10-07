package edu.uees.reservas.service;

/**
 * Se lanza al intentar crear una reserva con un id que ya existe.
 * Antes de Ae7 la reserva anterior se reemplazaba en silencio.
 */
public class ReservaDuplicadaException extends RuntimeException {

    private final String id;

    public ReservaDuplicadaException(String id) {
        super("Ya existe una reserva con ese id");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
