# Ae7 · Proyecto final: sistema de reservas de tutorías

Autora: Andrea Illescas (trabajo individual; asumo los cinco roles de la
defensa). Rama final: `ae7/proyecto-final` · Pull Request hacia `main`.

## 1. Problema y objetivo

Un sistema que permita crear reservas de tutorías, consultarlas, confirmarlas,
saber si todavía se pueden cancelar y calcular su total según el tipo de
cliente, expuesto como API REST. El objetivo de Ae7 no es solo que funcione:
es demostrar que el diseño es mantenible y que cada decisión está respaldada
por pruebas y por el historial de Git.

## 2. Arquitectura

```
HTTP ──> api/ReservaController ──> service/ReservaService ──> repository/ReservaRepository
              │  (DTO, validación)        │  (reglas)                 └─ ReservaRepositoryMemoria
              └─ api/ManejadorErrores      ├─> tarifa/CalculadoraTarifas ──> PoliticaTarifa (Strategy)
                 (excepción → HTTP)        └─> domain/Reserva, EstadoReserva
```

| Capa | Clases | Responsabilidad | No hace |
|---|---|---|---|
| API | `ReservaController`, `ManejadorErrores`, `CrearReservaRequest`, `TotalResponse` | Recibir peticiones, validar formato, elegir código HTTP | Reglas de negocio |
| Servicio | `ReservaService`, excepciones de negocio | Reglas: cancelación ≥ 2 h, crear sin duplicados, confirmar, total | Nada de HTTP |
| Tarifa | `PoliticaTarifa`, `TarifaVip`, `TarifaEstudiante`, `TarifaNormal`, `CalculadoraTarifas` | Una regla de precio por tipo | Conocer reservas o HTTP |
| Dominio | `Reserva`, `EstadoReserva` | Proteger su estado: id obligatorio, tipo por defecto, transiciones | Conocer servicios |
| Repositorio | `ReservaRepository`, `ReservaRepositoryMemoria` | Guardar y buscar | Reglas |

## 3. Endpoints

| Método | Ruta | Éxito | Errores |
|---|---|---|---|
| GET | `/api/reservas/salud` | 200 | — |
| GET | `/api/reservas/puede-cancelar?horas=N` | 200 `true`/`false` | 400 si N no es número |
| POST | `/api/reservas` | 201 | 400 con el campo que falta; 409 si el id existe |
| GET | `/api/reservas/{id}` | 200 | 404 |
| GET | `/api/reservas/{id}/total?base=N` | 200 `{id, base, total}` | 400 si N < 0; 404 |
| POST | `/api/reservas/{id}/confirmar` | 200 | 404 |

## 4. Patrones

| Patrón | Dónde | Problema que resuelve |
|---|---|---|
| **Strategy** | `tarifa/` | La tarifa varía por tipo (VIP 15 %, ESTUDIANTE 10 %, resto sin descuento). Con `if` por tipo, cada tipo nuevo obligaba a editar el servicio. Ahora es una clase nueva; `CalculadoraTarifasTest` lo demuestra con un tipo DOCENTE que existe solo en la prueba. |
| **Repository** | `ReservaRepository` + `ReservaRepositoryMemoria` | El servicio depende de una interfaz, no del mapa en memoria: cambiar a base de datos no toca las reglas. Permite usar un Mock en las pruebas del servicio. |
| **DTO** | `CrearReservaRequest`, `TotalResponse` | Separa el formato de la API del dominio; la validación de formato (`@NotBlank`) no ensucia `Reserva`. |
| **Inyección de dependencias** | constructores de `ReservaService`, `ReservaController`, `CalculadoraTarifas` | Bajo acoplamiento: Spring arma el grafo y las pruebas pasan dobles por constructor. |
| **Traducción centralizada de excepciones** (`@RestControllerAdvice`) | `ManejadorErrores` | El servicio lanza excepciones con significado de negocio; un solo lugar decide 400/404/409. |

### Patrones que evité y por qué

| Patrón | Por qué no |
|---|---|
| **State** para `EstadoReserva` | Solo hay tres estados y dos transiciones sin reglas propias; un enum basta. Sería un patrón sin problema que resolver. |
| **Builder** para `Reserva` | Tiene dos campos de entrada; un constructor es más claro. |
| **Observer** para notificaciones | No hay ningún requisito de notificar; agregarlo sería funcionalidad inventada. |
| **Factory** para las tarifas | Spring ya descubre las políticas como `@Component` e inyecta la lista; una fábrica duplicaría eso. |

## 5. Refactorizaciones relevantes

| Refactorización | Commit | Qué deuda redujo | Cómo se verificó |
|---|---|---|---|
| Extraer la traducción de errores a `ManejadorErrores` | `feat: responder 404 al buscar una reserva inexistente` (Actividad 2) | El 500 por id inexistente; el Controller no tenía dónde manejar errores sin llenarse de `try/catch` | Prueba de la capa HTTP que primero documentaba el 500 y después exige el 404 |
| Reemplazar `IllegalArgumentException` genérica por excepciones de negocio | Actividad 2 y Ae7 | Una misma excepción significaba «id vacío» y «no existe» | `ReservaNoEncontradaException` y `ReservaDuplicadaException` con sus pruebas |
| **Replace Conditional with Polymorphism** (Strategy) | `refactor: reemplazar el condicional de tarifas por el patron Strategy` | Porcentajes y tipos mezclados en el servicio | Las mismas 40 pruebas en verde sin cambiar ninguna aserción (antes y después en `docs/ae7/refactorizacion/`) |

## 6. Pruebas y cobertura

44 pruebas, todas en verde (`mvn clean test`):

| Clase | Pruebas | Tipo |
|---|---|---|
| `ReservaServiceTest` | 18 | Unitarias con Mock del repositorio: cancelación, crear, duplicado, buscar, confirmar, tarifas |
| `ReservaControllerTest` | 14 | Capa HTTP con `@WebMvcTest`: códigos y cuerpos de 200, 201, 400, 404, 409 |
| `ReservaApiIntegracionTest` | 5 | Aplicación completa sin dobles |
| `CalculadoraTarifasTest` | 4 | Cada política y la extensión con un tipo nuevo |
| `ReservaTest` | 3 | Dominio |

**JaCoCo:** 98 % de instrucciones (400/407) y **100 % de ramas** (12/12).

Qué significa: todas las decisiones del código (cada `if`, cada rama) se
ejecutan en alguna prueba. Lo que falta son 7 instrucciones:
`ReservasApplication.main`, que solo arranca Spring, y
`TarifaNormal.aplicaA()`, que nunca se llama porque `TarifaNormal` se usa como
valor por defecto y no compite en la elección. No agregué una prueba solo para
pintarlo de verde: no protegería ninguna regla.

La cobertura alta no garantiza que el código sea correcto: en la Ae6 encontré
un error que convivía con 100 % de cobertura. Lo que protege es que cada
prueba afirma un comportamiento concreto.

**Prueba que protege el mayor riesgo:**
`postConIdDuplicadoResponde409YConservaLaOriginal` (integración). Antes, un
POST repetido sobrescribía una reserva sin aviso: se perdían datos en
silencio. Esta prueba recorre la cadena real y comprueba que la original no
cambia.

## 7. Git y trazabilidad

| Rama | Contenido |
|---|---|
| `main` | Proyecto base + Actividad 2 |
| `feature/integracion-api` | Actividad 2: pruebas, reto del 404 |
| `audit/calidad-trazabilidad` | Actividad 3: auditoría y mejoras |
| `release/candidato-final` | Versión candidata de la Actividad 3 |
| `ae7/proyecto-final` | Ae7: pendientes, Strategy, documentación final |

Cada commit es un paso verificado con `mvn clean test`; los mensajes siguen
el formato `tipo: descripción` (`feat`, `test`, `refactor`, `docs`,
`release`). La refactorización Strategy está en su propio commit, separada
del commit que introdujo la regla, para que el diff muestre solo el cambio
de diseño.

## 8. Limitaciones y siguiente iteración

1. **Repositorio en memoria:** los datos se pierden al reiniciar. Siguiente
   paso: JPA con una base de datos; gracias a la interfaz `ReservaRepository`,
   el servicio no cambia.
2. **El tipo es texto libre:** `"PREMIUM"` se acepta y paga tarifa normal. Se
   podría validar contra los tipos conocidos con un enum.
3. **Se puede confirmar una reserva cancelada:** `Reserva` no restringe
   transiciones. Si el negocio lo pide, ese sería el momento de evaluar State.
4. **El total no se guarda en la reserva:** se calcula a pedido con una base
   que envía el cliente.
5. **Entorno:** en mi equipo, `mvn clean` puede fallar por carpetas de solo
   lectura (documentado en el README con la solución).

## 9. Declaración de uso de IA

Herramienta: un asistente de inteligencia artificial. Propósito: apoyo para
organizar la documentación, revisar la redacción y contrastar alternativas de
diseño. El código, las pruebas y las decisiones fueron revisados, ejecutados
y comprendidos por mí: ejecuté cada prueba, cada demo y cada commit en mi
equipo y puedo explicar cada parte del proyecto.
