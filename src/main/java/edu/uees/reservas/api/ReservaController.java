package edu.uees.reservas.api;

import edu.uees.reservas.domain.Reserva;
import edu.uees.reservas.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping("/salud")
    public ResponseEntity<String> salud() {
        return ResponseEntity.ok("API activa");
    }

    @GetMapping("/puede-cancelar")
    public ResponseEntity<Boolean> puedeCancelar(@RequestParam int horas) {
        return ResponseEntity.ok(service.puedeCancelar(horas));
    }

    @PostMapping
    public ResponseEntity<Reserva> crear(
            @Valid @RequestBody CrearReservaRequest request) {
        Reserva reserva = service.crear(request.id(), request.tipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> buscar(@PathVariable String id) {
        return ResponseEntity.ok(service.buscar(id));
    }
}
