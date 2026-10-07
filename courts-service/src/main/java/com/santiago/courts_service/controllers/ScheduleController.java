package com.santiago.courts_service.controllers;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santiago.courts_service.entities.Schedule;
import com.santiago.courts_service.services.ScheduleService;

@RestController
public class ScheduleController {

    final private ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @GetMapping("/courts/{courtId}/schedules")
    public ResponseEntity<?> listByCourt(@PathVariable Long courtId,
            @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(service.findByCourt(courtId, date));
    }

    @GetMapping("/courts/{courtId}/availability")
    public ResponseEntity<?> availability(@PathVariable Long courtId, @RequestParam LocalDate date) {
        return ResponseEntity.ok(service.findAvailable(courtId, date));
    }

    @PostMapping("/courts/{courtId}/schedules")
    public ResponseEntity<?> create(@PathVariable Long courtId, @RequestBody Schedule schedule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(courtId, schedule));
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Schedule> scheduleOptional = service.findById(id);
        if (scheduleOptional.isPresent()) {
            return ResponseEntity.ok(scheduleOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Optional<Schedule> scheduleOptional = service.update(id, schedule);
        if (scheduleOptional.isPresent()) {
            return ResponseEntity.ok(scheduleOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (service.delete(id).isPresent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoints internos que usa reservations-service para mover el estado del horario.

    @PutMapping("/schedules/{id}/hold")
    public ResponseEntity<?> hold(@PathVariable Long id) {
        return ResponseEntity.ok(service.hold(id));
    }

    @PutMapping("/schedules/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id) {
        return ResponseEntity.ok(service.reserve(id));
    }

    @PutMapping("/schedules/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id) {
        return ResponseEntity.ok(service.release(id));
    }

}
