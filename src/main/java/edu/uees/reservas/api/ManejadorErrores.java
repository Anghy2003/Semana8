package edu.uees.reservas.api;

import edu.uees.reservas.service.ReservaDuplicadaException;
import edu.uees.reservas.service.ReservaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce excepciones del servicio a respuestas HTTP.
 *
 * Vive en la capa api para que el Controller siga delgado y el Service no
 * dependa de HTTP.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ReservaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> reservaNoEncontrada(
            ReservaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage(), "id", ex.getId()));
    }

    @ExceptionHandler(ReservaDuplicadaException.class)
    public ResponseEntity<Map<String, String>> reservaDuplicada(
            ReservaDuplicadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage(), "id", ex.getId()));
    }
}
