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
        // Primero el dominio valida el id; despues se busca el duplicado.
        Reserva nueva = new Reserva(id, tipo);
        if (repository.buscarPorId(id).isPresent()) {
            throw new ReservaDuplicadaException(id);
        }
        return repository.guardar(nueva);
    }

    public Reserva buscar(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));
    }

    public Reserva confirmar(String id) {
        Reserva reserva = buscar(id);
        reserva.confirmar();
        return repository.guardar(reserva);
    }
}
