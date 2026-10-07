package edu.uees.reservas.tarifa;

import org.springframework.stereotype.Component;

/** ESTUDIANTE: 10 % de descuento. */
@Component
public class TarifaEstudiante implements PoliticaTarifa {

    @Override
    public boolean aplicaA(String tipo) {
        return "ESTUDIANTE".equalsIgnoreCase(tipo);
    }

    @Override
    public double aplicar(double totalBase) {
        return totalBase * 0.90;
    }
}
