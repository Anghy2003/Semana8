# UEES UCOM0310 — Semana 8 — Proyecto base

Proyecto base para las actividades de integración final.

## Requisitos
- Java 21
- Maven
- Git

## Ejecutar pruebas
```bash
mvn clean test
```

## Ejecutar aplicación
```bash
mvn spring-boot:run
```

## Endpoint inicial
`GET http://localhost:8080/api/reservas/salud`

## Cobertura
Después de `mvn clean test`, abrir:
`target/site/jacoco/index.html`

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
