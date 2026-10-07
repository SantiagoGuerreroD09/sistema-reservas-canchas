package com.santiago.reservations_service.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.santiago.reservations_service.entities.Reservation;
import com.santiago.reservations_service.entities.ReservationStatus;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByCreatedAtDesc();

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByStatusAndHoldExpiresAtLessThanEqual(ReservationStatus status, LocalDateTime now);

    // Cambio de estado condicional: si otra peticion (o el job de expiracion) ya movio
    // la reserva, devuelve 0 y no se pisa ese cambio.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Reservation r set r.status = :to where r.id = :id and r.status = :from")
    int changeStatus(@Param("id") Long id, @Param("from") ReservationStatus from,
            @Param("to") ReservationStatus to);
}
