package edu.uees.reservas.repository;

import edu.uees.reservas.domain.Reserva;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReservaRepositoryMemoria implements ReservaRepository {

    private final Map<String, Reserva> datos = new ConcurrentHashMap<>();

    @Override
    public Reserva guardar(Reserva reserva) {
        datos.put(reserva.getId(), reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }
}
