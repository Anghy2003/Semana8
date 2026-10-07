# Guion de defensa · Ae7 (≈ 14 minutos)

Trabajo individual: asumo los cinco roles (Integración, Diseño, Calidad,
Trazabilidad, Demo).

## Antes de empezar (preparación, 5 min antes)

```powershell
cd semana8-integracion-api
git switch ae7/proyecto-final
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
mvn clean test                      # dejar a la vista: 44 pruebas, BUILD SUCCESS
start target\site\jacoco\index.html # dejar abierta la pestaña de JaCoCo
mvn spring-boot:run                 # en otra consola; esperar "Started"
```

Tener abiertos: la presentación, la consola con la app, JaCoCo, el Pull
Request en GitHub y `src/main/java/edu/uees/reservas/tarifa/`.

## Secuencia

| Bloque | Tiempo | Qué digo | Qué muestro |
|---|---|---|---|
| 1. Problema y objetivo | 1–2 min | Sistema de reservas de tutorías como API REST: crear, consultar, confirmar, cancelar con 2 h o más y calcular el total por tipo. El objetivo es que sea mantenible y que cada decisión tenga evidencia. | Diapositiva 2 |
| 2. Arquitectura | 2–3 min | Controller → Service → Domain. El Controller solo traduce HTTP; las reglas están en el Service; la tarifa en su propio paquete; `Reserva` se protege sola. El repositorio es una interfaz. | Diapositiva 3 y el paquete `api` vs `service` |
| 3. Patrones y refactorización | 2 min | Strategy para la tarifa: primero la regla con `if` y 11 pruebas; después la refactoricé a una clase por tipo con las mismas 40 pruebas en verde. Patrones que evité: State, Builder, Observer. | Diapositivas 4–5; diff `ReservaService-antes` vs `después` |
| 4. Demo funcional | 3–4 min | Recorro los flujos principales y los errores. | `powershell -ExecutionPolicy Bypass -File docs\ae7\demo.ps1` |
| 5. Pruebas y cobertura | 2 min | 44 pruebas en cinco clases; 98 % de instrucciones y 100 % de ramas; por qué no persigo el 100 % de instrucciones. | Una prueba (`postConIdDuplicadoResponde409...`) y JaCoCo |
| 6. Git y trazabilidad | 1 min | Una rama por etapa; commits pequeños; la refactorización en su propio commit; PR documentado. | `git log --oneline --decorate -15` y el PR |
| 7. Limitaciones y cierre | 1–2 min | Repositorio en memoria, tipo como texto libre, confirmar una cancelada. Qué haría después. | Diapositiva 9 |

## Evidencias obligatorias durante la defensa

- [ ] Ejecutar al menos un endpoint → `demo.ps1` (10 pasos).
- [ ] Mostrar una prueba automatizada → `ReservaApiIntegracionTest.postConIdDuplicadoResponde409YConservaLaOriginal`.
- [ ] Mostrar reporte JaCoCo → `target/site/jacoco/index.html` y `TarifaNormal`.
- [ ] Mostrar git log y PR.
- [ ] Mostrar una decisión de diseño → `CalculadoraTarifas` + `PoliticaTarifa`.
- [ ] Explicar una limitación real → repositorio en memoria / tipo libre.

## Preguntas posibles y respuestas

**¿Qué decisión de diseño fue más importante?**
Separar la traducción de errores en `ManejadorErrores`. Gracias a eso el
servicio lanza excepciones con significado de negocio (no encontrada,
duplicada) y no sabe nada de HTTP, y el Controller sigue sin `try/catch`.
Resolvió el 500 de la Actividad 2 y permitió agregar el 409 y los 400 con
detalle sin tocar el Controller.

**¿Qué patrón resolvió un problema concreto?**
Strategy. La tarifa varía por tipo de cliente; con `if`, cada tipo nuevo
obligaba a modificar `ReservaService`. Ahora un tipo nuevo es una clase que
implementa `PoliticaTarifa`. La prueba `unTipoNuevoSeAgregaConUnaClase...`
agrega DOCENTE sin tocar la calculadora.

**¿Qué patrón evitaron y por qué?**
State para el estado de la reserva. Hay tres estados y dos transiciones sin
reglas propias; un enum es suficiente. Lo evaluaría si el negocio pidiera
reglas como «no confirmar una cancelada».

**¿Qué refactorización redujo deuda técnica?**
Replace Conditional with Polymorphism en la tarifa. El servicio dejó de
conocer porcentajes; cada regla quedó aislada y probada por separado. Lo
demuestra que las 40 pruebas pasaron sin cambiar una sola aserción.

**¿Qué caso de prueba protege el mayor riesgo?**
`postConIdDuplicadoResponde409YConservaLaOriginal`. Antes, un id repetido
sobrescribía la reserva sin aviso: pérdida silenciosa de datos. La prueba usa
la aplicación completa y verifica que la original no cambia.

**¿Qué significa la cobertura obtenida?**
100 % de ramas: todas las decisiones del código se ejecutan en alguna
prueba. 98 % de instrucciones: falta el `main` y `TarifaNormal.aplicaA()`,
que nunca se llama por diseño. La cobertura dice qué se ejecutó, no qué se
comprobó; por eso cada prueba tiene aserciones sobre el comportamiento.

**¿Qué evidencia Git demuestra la evolución?**
Las ramas `feature`, `audit`, `release` y `ae7`, una por etapa, y los
commits separados: la regla de tarifa en un `feat:` y su refactorización en
un `refactor:` aparte. En el diff del commit `refactor` se ve solo el cambio
de diseño.

**¿Qué mejorarían en una siguiente iteración?**
Persistencia real con JPA (el servicio no cambiaría gracias a la interfaz
`ReservaRepository`), validar el tipo contra los tipos conocidos y definir si
se permite confirmar una reserva cancelada.
