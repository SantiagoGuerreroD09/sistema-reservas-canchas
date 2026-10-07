package com.santiago.reservations_service.services;

import java.util.List;
import java.util.Optional;

import com.santiago.reservations_service.entities.Reservation;
import com.santiago.reservations_service.models.ReservationDetail;

public interface ReservationService {

    List<Reservation> findAll();

    List<Reservation> findByUser(Long userId);

    Optional<Reservation> findById(Long id);

    Optional<ReservationDetail> findDetailById(Long id);

    Reservation create(Reservation reservation);

    Reservation confirm(Long id);

    Reservation cancel(Long id);

    Reservation cancelByAdmin(Long id);

    int expirePendingHolds();
}
