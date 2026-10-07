package edu.uees.reservas.domain;

public class Reserva {

    private final String id;
    private final String tipo;
    private EstadoReserva estado;

    public Reserva(String id, String tipo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id obligatorio");
        }
        this.id = id;
        this.tipo = tipo == null ? "NORMAL" : tipo;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public String getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void confirmar() {
        estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        estado = EstadoReserva.CANCELADA;
    }
}
