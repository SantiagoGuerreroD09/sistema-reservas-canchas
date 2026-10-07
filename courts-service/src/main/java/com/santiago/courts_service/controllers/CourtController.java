package com.santiago.courts_service.controllers;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.santiago.courts_service.entities.Court;
import com.santiago.courts_service.services.CourtService;

@RestController
@RequestMapping("/courts")
public class CourtController {

    final private CourtService service;

    public CourtController(CourtService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String name,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(service.findAll(name, location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Court> courtOptional = service.findById(id);
        if (courtOptional.isPresent()) {
            return ResponseEntity.ok(courtOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Court court) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(court));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Court court) {
        Optional<Court> courtOptional = service.update(id, court);
        if (courtOptional.isPresent()) {
            return ResponseEntity.ok(courtOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (service.delete(id).isPresent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
