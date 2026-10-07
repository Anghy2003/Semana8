package edu.uees.reservas.api;

/** Respuesta de GET /api/reservas/{id}/total. */
public record TotalResponse(String id, double base, double total) {}
