package edu.uees.reservas.tarifa;

/**
 * Tarifa por defecto: sin descuento. Se usa cuando ninguna otra politica
 * aplica (NORMAL o un tipo desconocido). No es un @Component para que no
 * compita con las demas al elegir.
 */
public class TarifaNormal implements PoliticaTarifa {

    @Override
    public boolean aplicaA(String tipo) {
        return true;
    }

    @Override
    public double aplicar(double totalBase) {
        return totalBase;
    }
}
