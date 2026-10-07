package com.santiago.courts_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.santiago.courts_service.entities.Review;

public interface ReviewRepository extends CrudRepository<Review, Long> {

    List<Review> findByCourt_IdOrderByCreatedAtDesc(Long courtId);

    boolean existsByReservationId(Long reservationId);

    @Modifying
    @Query("delete from Review r where r.court.id = :courtId")
    void deleteByCourtId(@Param("courtId") Long courtId);
}
