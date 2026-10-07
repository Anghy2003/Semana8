## Objetivo

Entregar la versión final del sistema de reservas de tutorías para Ae7: API REST con Spring Boot y arquitectura Controller → Service → Domain, que resuelve los pendientes de la auditoría y aplica el patrón Strategy para las tarifas mediante una refactorización protegida por pruebas.

## Cambios

- **409 en id duplicado:** antes un POST repetido sobrescribía la reserva en silencio.
- **400 con detalle:** indica qué campo o parámetro falló, y si el JSON está mal formado.
- **`POST /api/reservas/{id}/confirmar`:** el servicio ya existía, pero no tenía endpoint.
- **`GET /api/reservas/{id}/total?base=N`:** regla de tarifas (VIP −15 %, ESTUDIANTE −10 %).
- **Refactorización a Strategy:** el condicional de tarifas pasó a `PoliticaTarifa` + `CalculadoraTarifas`. Va en un commit `refactor:` propio; las 40 pruebas siguen en verde sin cambiar ninguna aserción.
- Documentación: `docs/ae7/01_PROYECTO_FINAL.md`, guion de defensa, script de demo y README.

## Casos incorporados

| Área | Casos |
|---|---|
| Duplicados | servicio lanza `ReservaDuplicadaException` sin guardar; HTTP 409; integración: la original se conserva |
| Validación | id vacío, tipo ausente, `horas=abc`, JSON mal formado → 400 con detalle |
| Confirmar | 200 con CONFIRMADA; 404; integración: el estado queda guardado |
| Tarifas | NORMAL, VIP, ESTUDIANTE, minúsculas, tipo desconocido, base 0, base negativa, id inexistente |
| Strategy | elección por tipo, tarifa por defecto, tipo nuevo (DOCENTE) sin tocar la calculadora |

## Cómo verificar

```bash
git switch ae7/proyecto-final
mvn clean test          # 44 pruebas, BUILD SUCCESS
mvn spring-boot:run
powershell -ExecutionPolicy Bypass -File docs\ae7\demo.ps1
```

Lo verifiqué también en un clon limpio desde GitHub: 44 de 44 (`docs/ae7/evidencia/08-clon-limpio.txt`).

## Cobertura

JaCoCo: **98 % de instrucciones (400/407) y 100 % de ramas (12/12)**. Faltan `ReservasApplication.main` (solo arranca Spring) y `TarifaNormal.aplicaA()`, que no se llama por diseño porque es la tarifa por defecto. No agregué pruebas solo para cubrir esas líneas.

## Limitaciones

- Repositorio en memoria: los datos se pierden al reiniciar.
- El tipo es texto libre: un tipo desconocido paga tarifa normal.
- Se puede confirmar una reserva cancelada.
- En mi equipo, `mvn clean` puede fallar por carpetas de solo lectura (README, con la solución).

## Uso de IA

Usé un asistente de inteligencia artificial como apoyo para organizar la documentación, revisar la redacción y contrastar alternativas de diseño. Revisé, ejecuté y comprendí el código, las pruebas y las decisiones, y puedo explicar cada parte del proyecto.
