# Reflexión: separación de responsabilidades

En este laboratorio la API quedó en tres capas, y cada una tiene un motivo
distinto para cambiar:

| Capa | Clase | Qué hace | Qué no hace |
|---|---|---|---|
| Controller | `ReservaController`, `ManejadorErrores` | Recibe la petición, valida el formato (`@Valid`, tipo del parámetro), llama al servicio y elige el código HTTP | No decide reglas de negocio |
| Service | `ReservaService` | Aplica las reglas: cancelar con 2 h o más, crear, buscar, confirmar | No sabe que existe HTTP |
| Domain | `Reserva` | Protege su propio estado: id obligatorio, tipo por defecto, transiciones | No sabe que existe un servicio ni una API |

Lo comprobé con las pruebas, no solo leyendo el código. Las pruebas del
Service (`ReservaServiceTest`) corren sin Spring y sin servidor: la regla de
las 2 horas se prueba con un `int` y un `boolean`. Las del Controller
(`ReservaControllerTest`) reemplazan el Service por un doble, así que si una
falla sé que el problema está en la traducción HTTP y no en la regla.

El reto mostró por qué importa. Buscar un id inexistente daba 500 porque la
excepción del Service no tenía traducción HTTP. La solución no fue meter un
`if` en el Controller ni hacer que el Service devuelva un código HTTP: el
Service lanza una excepción con significado de negocio (`ReservaNoEncontradaException`)
y un `@RestControllerAdvice` la traduce a 404 en un solo lugar. Si mañana la
misma regla se usa desde otra interfaz que no sea REST, el Service no cambia.

Lo que me queda pendiente es lo mismo que me llevo como aprendizaje: la
validación de formato (id vacío) vive en el DTO con `@NotBlank`, y la
validación de negocio (id obligatorio) vive en `Reserva`. Están duplicadas a
propósito, porque protegen cosas distintas: la primera responde rápido con
400 al cliente; la segunda garantiza que una `Reserva` inválida no pueda
existir aunque alguien llame al servicio sin pasar por la API.
