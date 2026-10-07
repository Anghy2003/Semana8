# Reto · `GET /api/reservas/{id}`

## Comportamiento actual (proyecto base)

El endpoint ya existe en el proyecto base:

```java
@GetMapping("/{id}")
public ResponseEntity<Reserva> buscar(@PathVariable String id) {
    return ResponseEntity.ok(service.buscar(id));
}
```

y el servicio lanza una excepción cuando no encuentra el id:

```java
return repository.buscarPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
```

Lo probé con la aplicación corriendo (`docs/evidencia/02-curl-comportamiento-inicial.txt`):

| Petición | Respuesta |
|---|---|
| `GET /api/reservas/R-001` (existe) | **200** `{"id":"R-001","tipo":"NORMAL","estado":"PENDIENTE"}` |
| `GET /api/reservas/R-999` (no existe) | **500** `{"status":500,"error":"Internal Server Error",...}` |

**El problema:** pedir una reserva que no existe es un error *del cliente*,
pero la API responde 500, que significa «el servidor falló». Quien consume la
API no puede distinguir «esa reserva no existe» de «el sistema se cayó», y el
cuerpo no dice qué pasó. Ocurre porque nadie traduce la excepción del
servicio a una respuesta HTTP: Spring la trata como un error inesperado.

La prueba `getDeUnIdInexistenteHoyNoTieneManejoHttp()` deja documentado
este comportamiento antes de cambiarlo.

## Mejora propuesta

1. El servicio lanza una excepción propia, `ReservaNoEncontradaException`,
   en lugar de una `IllegalArgumentException` genérica (que también usa el
   dominio para otros errores, como el id vacío).
2. Un `@RestControllerAdvice` en la capa `api` traduce esa excepción a
   **404 Not Found** con un cuerpo claro: `{"error":"Reserva no encontrada","id":"R-999"}`.

Así el Service sigue sin saber nada de HTTP (solo dice «no existe») y el
Controller sigue delgado: la traducción a códigos HTTP vive en un solo lugar
de la capa web.

## Después de la mejora

Implementada en el commit `feat: responder 404 al buscar una reserva inexistente`
(`ReservaNoEncontradaException` + `ManejadorErrores`). Con la aplicación
corriendo (`docs/evidencia/08-curl-reto-despues.txt`):

| Petición | Respuesta |
|---|---|
| `GET /api/reservas/R-001` (existe) | **200** `{"id":"R-001","tipo":"NORMAL","estado":"PENDIENTE"}` |
| `GET /api/reservas/R-999` (no existe) | **404** `{"id":"R-999","error":"Reserva no encontrada"}` |

La prueba de la capa HTTP pasó de documentar el problema
(`getDeUnIdInexistenteHoyNoTieneManejoHttp`) a exigir la solución
(`getDeUnIdInexistenteResponde404ConMensaje`).

## Mejoras futuras que no hice

- **Errores de validación sin detalle.** Un POST con `id` vacío responde 400,
  pero el cuerpo no dice qué campo falló. Se puede agregar al mismo
  `ManejadorErrores` un handler de `MethodArgumentNotValidException`.
- **Id duplicado.** Un POST con un id que ya existe reemplaza la reserva
  anterior en silencio y responde 201. Debería responder 409 Conflict.
- **Confirmar por HTTP.** `ReservaService.confirmar(id)` existe pero no tiene
  endpoint.
