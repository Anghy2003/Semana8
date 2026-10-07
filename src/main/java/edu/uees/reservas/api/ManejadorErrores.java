package edu.uees.reservas.api;

import edu.uees.reservas.service.ReservaDuplicadaException;
import edu.uees.reservas.service.ReservaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

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

    /** Body con campos vacios: dice cuales fallaron y por que. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(
            MethodArgumentNotValidException ex) {
        Map<String, String> campos = new TreeMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Datos invalidos", "campos", campos));
    }

    /** Parametro con tipo incorrecto, por ejemplo horas=abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Parametro invalido",
                        "parametro", ex.getName(),
                        "valor", String.valueOf(ex.getValue())));
    }

    /** Argumento invalido para una regla de negocio, por ejemplo base negativa. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /** JSON mal formado o ausente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> cuerpoIlegible(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "El cuerpo de la peticion no es un JSON valido"));
    }
}
