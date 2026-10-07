package edu.uees.reservas.repository;

import edu.uees.reservas.domain.Reserva;
import java.util.Optional;

public interface ReservaRepository {
    Reserva guardar(Reserva reserva);
    Optional<Reserva> buscarPorId(String id);
}
