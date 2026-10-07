package edu.uees.reservas.tarifa;

/**
 * Strategy: cada tipo de reserva tiene su propia regla de tarifa.
 *
 * Reemplaza los if por tipo que tenia ReservaService.calcularTotal. Para un
 * tipo nuevo se agrega una clase que implemente esta interfaz; el servicio
 * no cambia.
 */
public interface PoliticaTarifa {

    /** Indica si esta politica corresponde al tipo de reserva recibido. */
    boolean aplicaA(String tipo);

    /** Calcula el total a partir de la tarifa base. */
    double aplicar(double totalBase);
}
