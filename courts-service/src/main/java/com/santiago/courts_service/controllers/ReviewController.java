package com.santiago.courts_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.santiago.courts_service.entities.Review;
import com.santiago.courts_service.services.ReviewService;

@RestController
public class ReviewController {

    final private ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping("/courts/{courtId}/reviews")
    public ResponseEntity<?> listByCourt(@PathVariable Long courtId) {
        return ResponseEntity.ok(service.findByCourt(courtId));
    }

    @PostMapping("/courts/{courtId}/reviews")
    public ResponseEntity<?> create(@PathVariable Long courtId, @RequestBody Review review) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(courtId, review));
    }

    // Moderacion (administrador).
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (service.delete(id).isPresent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
