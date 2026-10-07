# Sistema de reservas de tutorías · API REST (Spring Boot)

Proyecto integrador de Diseño de Software (UCOM0310), Semana 8.
Autora: Andrea Illescas · Repositorio: https://github.com/Anghy2003/Semana8

API REST con separación **Controller → Service → Domain** y un repositorio en
memoria.

## Requisitos
- Java 21
- Maven 3.9+
- Git

## Ejecutar pruebas
```bash
mvn clean test          # 44 pruebas: servicio, capa HTTP, integración, tarifas y dominio
```
Reporte de cobertura: `target/site/jacoco/index.html` (98 % de instrucciones, 100 % de ramas).

## Ejecutar la aplicación
```bash
mvn spring-boot:run     # http://localhost:8080
```

## Endpoints
| Método | Ruta | Éxito | Errores |
|---|---|---|---|
| GET | `/api/reservas/salud` | 200 `API activa` | — |
| GET | `/api/reservas/puede-cancelar?horas=N` | 200 `true` si N ≥ 2 | 400 si N no es número |
| POST | `/api/reservas` con `{"id":"R-001","tipo":"NORMAL"}` | 201 con la reserva | 400 con el campo que falta; 409 si el id ya existe |
| GET | `/api/reservas/{id}` | 200 con la reserva | 404 |
| GET | `/api/reservas/{id}/total?base=N` | 200 `{id, base, total}` (VIP −15 %, ESTUDIANTE −10 %) | 400 si N < 0; 404 |
| POST | `/api/reservas/{id}/confirmar` | 200 con estado CONFIRMADA | 404 |

## Demostración
Con la aplicación corriendo:
```powershell
powershell -ExecutionPolicy Bypass -File docse7\demo.ps1                    # demo de la defensa (10 pasos)
powershell -ExecutionPolicy Bypass -File docsuditoriauditoria-funcional.ps1  # 9 de 9 OK
```

## Diseño
Controller → Service → Domain, con el cálculo de tarifas como **Strategy**
(`tarifa/`). Detalle de arquitectura, patrones usados y evitados,
refactorizaciones, pruebas y limitaciones en
[`docs/ae7/01_PROYECTO_FINAL.md`](docs/ae7/01_PROYECTO_FINAL.md); guion de la
defensa en [`docs/ae7/02_GUION_DEFENSA.md`](docs/ae7/02_GUION_DEFENSA.md).

## Limitaciones conocidas
- Repositorio en memoria: los datos se pierden al reiniciar.
- El tipo es texto libre: un tipo desconocido paga tarifa normal.
- Se puede confirmar una reserva cancelada.

## Problema conocido del entorno (Windows)
En el equipo donde se desarrolló, después de `mvn test` algunas carpetas de
`target/classes` quedan con el atributo de **solo lectura** y el siguiente
`mvn clean` falla con `Failed to delete ...	arget\classes\...`. No es un
defecto del código (ocurre también en otros proyectos del mismo equipo). Si
pasa, borrar `target` y volver a ejecutar:
```powershell
Remove-Item -Recurse -Force target; mvn clean test
```

## Documentación
| Archivo | Contenido |
|---|---|
| `docs/04_RETO_BUSQUEDA.md` | Búsqueda por id: 500 antes, 404 después |
| `docs/05_REFLEXION.md` | Separación de responsabilidades |
| `docs/02_AUDITORIA_FINAL_PLANTILLA.md` | Checklist de auditoría de la Actividad 3 |
| `docs/auditoria/` | Evidencias de la auditoría: pruebas, JaCoCo antes/después, Git, capturas |
| `docs/ae7/` | Ae7: proyecto final, guion de defensa, demo, refactorización antes/después, evidencias |

---

## Actividad 2 · Laboratorio de integración Spring Boot

Autora: Andrea Illescas · Diseño de Software (UCOM0310) · Semana 8 · Rama `feature/integracion-api`

### Endpoints

| Método | Ruta | Capa que decide | Respuesta |
|---|---|---|---|
| GET | `/api/reservas/salud` | Controller | 200 `API activa` |
| GET | `/api/reservas/puede-cancelar?horas=N` | Service (`>= 2`) | 200 `true` / `false`; 400 si `horas` no es número |
| POST | `/api/reservas` (`{"id","tipo"}`) | DTO valida, Service crea | 201 con la reserva; 400 si falta `id` o `tipo` |
| GET | `/api/reservas/{id}` | Service busca, `ManejadorErrores` traduce | 200 con la reserva; **404** si no existe |

### Verificar

```bash
mvn clean test           # 16 pruebas: 9 del servicio, 7 de la capa HTTP
mvn spring-boot:run
curl "http://localhost:8080/api/reservas/puede-cancelar?horas=2"
curl -X POST http://localhost:8080/api/reservas -H "Content-Type: application/json" -d '{"id":"R-001","tipo":"NORMAL"}'
curl http://localhost:8080/api/reservas/R-999   # 404
```

### Documentación

- [`docs/04_RETO_BUSQUEDA.md`](docs/04_RETO_BUSQUEDA.md): búsqueda por id, comportamiento antes (500) y después (404), mejoras futuras.
- [`docs/05_REFLEXION.md`](docs/05_REFLEXION.md): separación Controller–Service–Domain.
- `docs/evidencia/` y `docs/capturas/`: salidas de curl, de `mvn clean test` y capturas de consola.
