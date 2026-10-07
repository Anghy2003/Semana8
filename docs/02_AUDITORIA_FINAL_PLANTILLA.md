# Auditoría final · Actividad 3

Rama auditada: `audit/calidad-trazabilidad` (desde `main`, que integra la
Actividad 2). Versión candidata: `release/candidato-final`.

## Funcional
- [x] Aplicación inicia — `mvn spring-boot:run`, Tomcat en el puerto 8080 (`auditoria/evidencia/02-arranque.txt`).
- [x] Endpoints principales responden — 9 de 9 comprobaciones con `auditoria-funcional.ps1` (`03-auditoria-funcional.txt`).
- [x] Errores conocidos documentados — id duplicado, 400 sin detalle, confirmar sin endpoint (README, «Limitaciones conocidas»).
- [x] Demostración repetible — script `docs/auditoria/auditoria-funcional.ps1`.

## Diseño
- [x] Controller delgado — solo traduce HTTP; los errores van a `ManejadorErrores`.
- [x] Service claro — reglas de negocio, sin dependencias de HTTP.
- [x] Dominio coherente — `Reserva` valida su id y su tipo por defecto.

## Pruebas
- [x] `mvn clean test` exitoso — 16 pruebas antes de la auditoría, **22** después.
- [x] Casos normales, límites (2 h / 1 h) y excepciones (id vacío, id inexistente).
- [x] Pruebas de Service, Controller, integración y dominio.
- [x] JaCoCo revisado — ver tabla siguiente.

| Elemento | Hallazgo | Riesgo | Mejora |
|---|---|---|---|
| Clase `ReservaRepositoryMemoria` | 0 % (0/5 líneas). Todas las pruebas usaban dobles: nunca se probó la cadena real Controller → Service → Repository. | Un error al guardar o buscar en el repositorio real no lo detecta ninguna prueba, aunque la API dependa de él. | `ReservaApiIntegracionTest` (SpringBootTest): crear y consultar, 404 real, y caracterización del id duplicado. Ahora 5/5. |
| Rama no cubierta en `Reserva` | Línea 10 (`id == null`) y línea 14 (`tipo == null`) a medias; `cancelar()` sin ejecutar. | Cambiar la validación del id o el tipo por defecto pasaría sin detectar. | `ReservaTest` (3 pruebas). Ramas del proyecto: 6/8 → 8/8. |

| Métrica JaCoCo | Antes | Después |
|---|---|---|
| Instrucciones | 83 % (176/211) | 97 % (206/211) |
| Ramas | 75 % (6/8) | 100 % (8/8) |
| `ReservaRepositoryMemoria` | 0 % | 100 % |
| Sin cubrir | repositorio, `cancelar()`, 2 ramas, `main` | solo `ReservasApplication.main` (arranque, no se prueba unitariamente) |

## Git
- [x] Sin `target/` ni archivos de IDE versionados (`.gitignore` correcto).
- [x] Sin secretos (búsqueda de `password`, `secret`, `token`, `api_key`, `ghp_` en todo el historial).
- [x] Commits descriptivos — ninguno genérico (`final`, `cambios`, `wip`).
- [x] README actualizado — antes se titulaba «Proyecto base» y solo mostraba `/salud`; ahora tiene endpoints, pruebas, limitaciones y el problema del entorno.
- [x] Rama final identificable — `release/candidato-final`.

## Problema real encontrado durante la auditoría

`mvn clean test` falló tres veces con `Failed to delete ...\target\classes\...`.
Primero pensé que era la aplicación todavía corriendo, pero volvió a fallar sin
la aplicación. Lo diagnostiqué paso a paso:
- después de `mvn compile`, `mvn clean` funciona siempre;
- después de `mvn test`, falla siempre;
- la carpeta queda con el atributo **solo lectura**;
- pasa también en el proyecto de la Semana 7.

Conclusión: es del entorno de mi equipo, no del código. Lo dejé documentado
en el README con la solución, porque afecta la reproducibilidad: otra persona
en el mismo entorno se encontraría con el mismo error.

## Limitaciones
1. Un POST con id repetido reemplaza la reserva en silencio (caracterizado en `postConIdDuplicadoHoyReemplazaLaReservaAnterior`; debería ser 409).
2. Los 400 de validación no dicen qué campo falló.
3. `confirmar(id)` no está expuesto por HTTP.
4. Repositorio en memoria: sin persistencia real.
