package com.santiago.courts_service.services;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santiago.courts_service.clients.ReservationFeignClient;
import com.santiago.courts_service.entities.Court;
import com.santiago.courts_service.entities.Review;
import com.santiago.courts_service.entities.Schedule;
import com.santiago.courts_service.exceptions.BadRequestException;
import com.santiago.courts_service.exceptions.ConflictException;
import com.santiago.courts_service.exceptions.NotFoundException;
import com.santiago.courts_service.models.Reservation;
import com.santiago.courts_service.repositories.CourtRepository;
import com.santiago.courts_service.repositories.ReviewRepository;
import com.santiago.courts_service.repositories.ScheduleRepository;

import feign.FeignException;

@Service
public class ReviewServiceImpl implements ReviewService {

    final private ReviewRepository repository;

    final private CourtRepository courtRepository;

    final private ScheduleRepository scheduleRepository;

    final private ReservationFeignClient reservationClient;

    final private Clock clock;

    public ReviewServiceImpl(ReviewRepository repository, CourtRepository courtRepository,
            ScheduleRepository scheduleRepository, ReservationFeignClient reservationClient, Clock clock) {
        this.repository = repository;
        this.courtRepository = courtRepository;
        this.scheduleRepository = scheduleRepository;
        this.reservationClient = reservationClient;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByCourt(Long courtId) {
        findCourt(courtId);
        return repository.findByCourt_IdOrderByCreatedAtDesc(courtId);
    }

    @Override
    @Transactional
    public Review save(Long courtId, Review review) {
        Court court = findCourt(courtId);
        if (review.getUserId() == null || review.getReservationId() == null) {
            throw new BadRequestException("El usuario y la reserva son obligatorios");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BadRequestException("La calificacion debe estar entre 1 y 5");
        }
        if (repository.existsByReservationId(review.getReservationId())) {
            throw new ConflictException("Esta reserva ya tiene una resena");
        }
        validateCompletedReservation(courtId, review);

        review.setId(null);
        review.setCourt(court);
        review.setCreatedAt(LocalDateTime.now(clock));
        return repository.save(review);
    }

    @Override
    @Transactional
    public Optional<Review> delete(Long id) {
        return repository.findById(id).map(review -> {
            repository.delete(review);
            return review;
        });
    }

    // Solo se puede resenar una reserva propia, confirmada, de esta cancha y cuyo horario ya termino.
    private void validateCompletedReservation(Long courtId, Review review) {
        Reservation reservation;
        try {
            reservation = reservationClient.details(review.getReservationId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("La reserva no existe");
        }
        if (!review.getUserId().equals(reservation.getUserId())) {
            throw new ConflictException("La reserva no pertenece a este usuario");
        }
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new ConflictException("Solo se pueden resenar reservas confirmadas");
        }
        Schedule schedule = scheduleRepository.findById(reservation.getScheduleId())
                .orElseThrow(() -> new NotFoundException("El horario de la reserva no existe"));
        if (!courtId.equals(schedule.getCourtId())) {
            throw new ConflictException("La reserva no corresponde a esta cancha");
        }
        LocalDateTime end = LocalDateTime.of(schedule.getDate(), schedule.getEndTime());
        if (end.isAfter(LocalDateTime.now(clock))) {
            throw new ConflictException("Solo se puede resenar despues de haber jugado");
        }
    }

    private Court findCourt(Long courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new NotFoundException("La cancha no existe"));
    }

}
