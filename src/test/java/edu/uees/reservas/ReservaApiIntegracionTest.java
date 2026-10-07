package edu.uees.reservas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion agregadas en la auditoria (Actividad 3).
 *
 * JaCoCo mostraba ReservaRepositoryMemoria en 0 %: las pruebas del servicio
 * usan un repositorio simulado y las del controller simulan el servicio, asi
 * que nunca se probaba la cadena real Controller -> Service -> Repository.
 * Aqui se levanta la aplicacion completa, sin dobles.
 *
 * El repositorio en memoria es compartido entre pruebas: cada una usa ids
 * propios para no depender del orden de ejecucion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReservaApiIntegracionTest {

    @Autowired
    private MockMvc mvc;

    private void crear(String id, String tipo) throws Exception {
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + id + "\",\"tipo\":\"" + tipo + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void unaReservaCreadaSePuedeConsultarPorSuId() throws Exception {
        // Arrange
        crear("INT-001", "ESTUDIANTE");

        // Act & Assert: el GET lee lo que guardo el repositorio real
        mvc.perform(get("/api/reservas/INT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("INT-001"))
                .andExpect(jsonPath("$.tipo").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void consultarUnIdQueNuncaSeCreoResponde404() throws Exception {
        // Act & Assert: el repositorio real devuelve vacio y llega como 404
        mvc.perform(get("/api/reservas/INT-NO-EXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reserva no encontrada"))
                .andExpect(jsonPath("$.id").value("INT-NO-EXISTE"));
    }

    @Test
    void postConIdDuplicadoHoyReemplazaLaReservaAnterior() throws Exception {
        // Limitacion conocida (docs/04_RETO_BUSQUEDA.md): se caracteriza el
        // comportamiento actual para que un cambio futuro a 409 sea visible.

        // Arrange
        crear("INT-002", "NORMAL");

        // Act
        crear("INT-002", "VIP");

        // Assert
        mvc.perform(get("/api/reservas/INT-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("VIP"));
    }
}
