package com.santiago.reservations_service.controllers;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santiago.reservations_service.entities.Reservation;
import com.santiago.reservations_service.models.ReservationDetail;
import com.santiago.reservations_service.services.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    // Sin userId: todas las reservas (administrador). Con userId: historial del usuario.
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(service.findByUser(userId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Reservation> reservationOptional = service.findById(id);
        if (reservationOptional.isPresent()) {
            return ResponseEntity.ok(reservationOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> fullDetail(@PathVariable Long id) {
        Optional<ReservationDetail> detailOptional = service.findDetailById(id);
        if (detailOptional.isPresent()) {
            return ResponseEntity.ok(detailOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Reservation reservation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(reservation));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @PutMapping("/{id}/admin-cancel")
    public ResponseEntity<?> cancelByAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelByAdmin(id));
    }

}
