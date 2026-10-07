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
    void unaReservaConfirmadaQuedaConfirmadaAlConsultarla() throws Exception {
        // Arrange
        crear("INT-003", "VIP");

        // Act
        mvc.perform(post("/api/reservas/INT-003/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        // Assert: el cambio quedo guardado en el repositorio real
        mvc.perform(get("/api/reservas/INT-003"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void elTotalDeUnEstudianteTieneDiezPorCientoDeDescuento() throws Exception {
        // Arrange
        crear("INT-004", "ESTUDIANTE");

        // Act & Assert
        mvc.perform(get("/api/reservas/INT-004/total").param("base", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(36.0));
    }

    @Test
    void postConIdDuplicadoResponde409YConservaLaOriginal() throws Exception {
        // Ae7: antes se reemplazaba en silencio (esta prueba lo caracterizaba
        // en la auditoria). Ahora es un cambio funcional deliberado.

        // Arrange
        crear("INT-002", "NORMAL");

        // Act
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"INT-002\",\"tipo\":\"VIP\"}"))
                .andExpect(status().isConflict());

        // Assert: la reserva original no cambio
        mvc.perform(get("/api/reservas/INT-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("NORMAL"));
    }
}
