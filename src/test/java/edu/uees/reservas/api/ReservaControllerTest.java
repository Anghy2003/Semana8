package edu.uees.reservas.api;

import edu.uees.reservas.domain.Reserva;
import edu.uees.reservas.service.ReservaDuplicadaException;
import edu.uees.reservas.service.ReservaNoEncontradaException;
import edu.uees.reservas.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa HTTP: solo se levanta el Controller (WebMvcTest) y el
 * Service se reemplaza por un doble. Comprueban que el Controller traduce
 * peticiones y respuestas sin contener reglas de negocio.
 */
@WebMvcTest(controllers = {ReservaController.class, ManejadorErrores.class})
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ReservaService service;

    @Test
    void saludRespondeApiActiva() throws Exception {
        mvc.perform(get("/api/reservas/salud"))
                .andExpect(status().isOk())
                .andExpect(content().string("API activa"));
    }

    @Test
    void puedeCancelarDelegaEnElServicioYDevuelveSuRespuesta() throws Exception {
        // Arrange
        when(service.puedeCancelar(2)).thenReturn(true);

        // Act & Assert
        mvc.perform(get("/api/reservas/puede-cancelar").param("horas", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        verify(service).puedeCancelar(2);
    }

    @Test
    void puedeCancelarConHorasNoNumericasResponde400() throws Exception {
        mvc.perform(get("/api/reservas/puede-cancelar").param("horas", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro invalido"))
                .andExpect(jsonPath("$.parametro").value("horas"))
                .andExpect(jsonPath("$.valor").value("abc"));
        verify(service, never()).puedeCancelar(anyInt());
    }

    @Test
    void postValidoCreaLaReservaYResponde201() throws Exception {
        // Arrange
        when(service.crear("R-001", "NORMAL")).thenReturn(new Reserva("R-001", "NORMAL"));

        // Act & Assert
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"R-001\",\"tipo\":\"NORMAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("R-001"))
                .andExpect(jsonPath("$.tipo").value("NORMAL"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void postConIdVacioResponde400SinLlamarAlServicio() throws Exception {
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"\",\"tipo\":\"NORMAL\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos invalidos"))
                .andExpect(jsonPath("$.campos.id").value("El id es obligatorio"));
        verify(service, never()).crear(anyString(), anyString());
    }

    @Test
    void postSinTipoIndicaElCampoQueFalta() throws Exception {
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"R-001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipo").value("El tipo es obligatorio"));
    }

    @Test
    void postConJsonMalFormadoResponde400() throws Exception {
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El cuerpo de la peticion no es un JSON valido"));
        verify(service, never()).crear(anyString(), anyString());
    }

    @Test
    void postConIdDuplicadoResponde409() throws Exception {
        // Arrange
        when(service.crear("R-001", "VIP")).thenThrow(new ReservaDuplicadaException("R-001"));

        // Act & Assert
        mvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"R-001\",\"tipo\":\"VIP\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.id").value("R-001"));
    }

    @Test
    void getPorIdDevuelveLaReserva() throws Exception {
        // Arrange
        when(service.buscar("R-002")).thenReturn(new Reserva("R-002", "VIP"));

        // Act & Assert
        mvc.perform(get("/api/reservas/R-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("R-002"))
                .andExpect(jsonPath("$.tipo").value("VIP"));
    }

    @Test
    void totalDevuelveBaseYTotalCalculadoPorElServicio() throws Exception {
        // Arrange
        when(service.calcularTotal("R-002", 40.0)).thenReturn(34.0);

        // Act & Assert
        mvc.perform(get("/api/reservas/R-002/total").param("base", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("R-002"))
                .andExpect(jsonPath("$.base").value(40.0))
                .andExpect(jsonPath("$.total").value(34.0));
    }

    @Test
    void totalConBaseNegativaResponde400ConMensaje() throws Exception {
        when(service.calcularTotal("R-002", -1.0))
                .thenThrow(new IllegalArgumentException("Total base invalido"));

        mvc.perform(get("/api/reservas/R-002/total").param("base", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Total base invalido"));
    }

    @Test
    void confirmarDevuelveLaReservaConfirmada() throws Exception {
        // Arrange
        Reserva confirmada = new Reserva("R-003", "NORMAL");
        confirmada.confirmar();
        when(service.confirmar("R-003")).thenReturn(confirmada);

        // Act & Assert
        mvc.perform(post("/api/reservas/R-003/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void confirmarUnIdInexistenteResponde404() throws Exception {
        when(service.confirmar("R-999")).thenThrow(new ReservaNoEncontradaException("R-999"));

        mvc.perform(post("/api/reservas/R-999/confirmar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDeUnIdInexistenteResponde404ConMensaje() throws Exception {
        // Reto: antes terminaba en HTTP 500 (docs/04_RETO_BUSQUEDA.md)
        when(service.buscar("R-999")).thenThrow(new ReservaNoEncontradaException("R-999"));

        mvc.perform(get("/api/reservas/R-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reserva no encontrada"))
                .andExpect(jsonPath("$.id").value("R-999"));
    }
}
