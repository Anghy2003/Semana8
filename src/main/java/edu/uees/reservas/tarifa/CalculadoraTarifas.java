package edu.uees.reservas.tarifa;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Contexto del Strategy: elige la politica que corresponde al tipo.
 * Spring inyecta todas las politicas registradas como @Component.
 */
@Component
public class CalculadoraTarifas {

    private final List<PoliticaTarifa> politicas;
    private final PoliticaTarifa porDefecto = new TarifaNormal();

    public CalculadoraTarifas(List<PoliticaTarifa> politicas) {
        this.politicas = List.copyOf(politicas);
    }

    public double calcular(String tipo, double totalBase) {
        return politicas.stream()
                .filter(p -> p.aplicaA(tipo))
                .findFirst()
                .orElse(porDefecto)
                .aplicar(totalBase);
    }
}
