# Notas de la versión candidata final

Rama: `release/candidato-final` · Origen: `audit/calidad-trazabilidad` (sobre `main`).

## Qué incluye
- API REST de reservas con Controller → Service → Domain: `/salud`, `/puede-cancelar`, `POST /api/reservas`, `GET /api/reservas/{id}`.
- 404 con mensaje claro al buscar una reserva inexistente (antes 500).
- 22 pruebas: servicio (9), capa HTTP con MockMvc (7), integración con la aplicación completa (3) y dominio (3).
- Cobertura JaCoCo: 97 % de instrucciones y 100 % de ramas.
- Script de auditoría funcional repetible: `docs/auditoria/auditoria-funcional.ps1`.

## Cómo verificar esta versión
```bash
git switch release/candidato-final
mvn clean test                 # 22 pruebas, BUILD SUCCESS
mvn spring-boot:run
powershell -ExecutionPolicy Bypass -File docs\auditoria\auditoria-funcional.ps1
```

## Pendiente para la entrega final (Ae7)
- Responder 409 ante un id duplicado.
- Detalle del campo en los errores 400.
- Endpoint para confirmar una reserva.
