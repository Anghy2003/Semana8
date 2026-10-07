package edu.uees.reservas.tarifa;

import org.springframework.stereotype.Component;

/** VIP: 15 % de descuento. */
@Component
public class TarifaVip implements PoliticaTarifa {

    @Override
    public boolean aplicaA(String tipo) {
        return "VIP".equalsIgnoreCase(tipo);
    }

    @Override
    public double aplicar(double totalBase) {
        return totalBase * 0.85;
    }
}
