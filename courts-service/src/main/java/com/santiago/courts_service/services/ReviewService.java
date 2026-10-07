package com.santiago.courts_service.services;

import java.util.List;
import java.util.Optional;

import com.santiago.courts_service.entities.Review;

public interface ReviewService {

    List<Review> findByCourt(Long courtId);

    Review save(Long courtId, Review review);

    Optional<Review> delete(Long id);
}
