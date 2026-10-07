package edu.uees.reservas.service;

/**
 * Se lanza cuando se pide una reserva cuyo id no existe.
 *
 * Es propia del servicio y no sabe nada de HTTP: la capa api decide que
 * codigo de respuesta le corresponde.
 */
public class ReservaNoEncontradaException extends RuntimeException {

    private final String id;

    public ReservaNoEncontradaException(String id) {
        super("Reserva no encontrada");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
