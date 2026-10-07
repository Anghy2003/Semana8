package edu.uees.reservas.service;

import edu.uees.reservas.domain.Reserva;
import edu.uees.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    private final ReservaRepository repository;

    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    public boolean puedeCancelar(int horasAnticipacion) {
        return horasAnticipacion >= 2;
    }

    public Reserva crear(String id, String tipo) {
        return repository.guardar(new Reserva(id, tipo));
    }

    public Reserva buscar(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
    }

    public Reserva confirmar(String id) {
        Reserva reserva = buscar(id);
        reserva.confirmar();
        return repository.guardar(reserva);
    }
}
