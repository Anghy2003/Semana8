package edu.uees.reservas.tarifa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraTarifasTest {

    private final CalculadoraTarifas calculadora =
            new CalculadoraTarifas(List.of(new TarifaVip(), new TarifaEstudiante()));

    @Test
    void eligeLaPoliticaVip() {
        assertEquals(85.0, calculadora.calcular("VIP", 100), 0.001);
    }

    @Test
    void eligeLaPoliticaEstudiante() {
        assertEquals(90.0, calculadora.calcular("ESTUDIANTE", 100), 0.001);
    }

    @Test
    void sinPoliticaQueApliqueUsaLaTarifaNormal() {
        assertEquals(100.0, calculadora.calcular("NORMAL", 100), 0.001);
        assertEquals(100.0, calculadora.calcular("PREMIUM", 100), 0.001);
        assertEquals(100.0, calculadora.calcular(null, 100), 0.001);
    }

    @Test
    void unTipoNuevoSeAgregaConUnaClaseSinTocarLaCalculadora() {
        // Arrange: politica que solo existe en esta prueba (Open/Closed)
        PoliticaTarifa docente = new PoliticaTarifa() {
            @Override
            public boolean aplicaA(String tipo) {
                return "DOCENTE".equalsIgnoreCase(tipo);
            }

            @Override
            public double aplicar(double totalBase) {
                return totalBase * 0.50;
            }
        };
        CalculadoraTarifas extendida = new CalculadoraTarifas(
                List.of(new TarifaVip(), new TarifaEstudiante(), docente));

        // Act
        double total = extendida.calcular("DOCENTE", 100);

        // Assert
        assertEquals(50.0, total, 0.001);
    }
}
