package com.santiago.reservations_service.services;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santiago.reservations_service.clients.CourtFeignClient;
import com.santiago.reservations_service.entities.Reservation;
import com.santiago.reservations_service.entities.ReservationStatus;
import com.santiago.reservations_service.exceptions.BadRequestException;
import com.santiago.reservations_service.exceptions.ConflictException;
import com.santiago.reservations_service.exceptions.NotFoundException;
import com.santiago.reservations_service.models.ReservationDetail;
import com.santiago.reservations_service.models.Schedule;
import com.santiago.reservations_service.repositories.ReservationRepository;

import feign.FeignException;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    final private ReservationRepository repository;

    final private CourtFeignClient courtClient;

    final private Clock clock;

    final private long cancellationMinHours;

    public ReservationServiceImpl(ReservationRepository repository, CourtFeignClient courtClient, Clock clock,
            @Value("${config.cancellation.min-hours}") long cancellationMinHours) {
        this.repository = repository;
        this.courtClient = courtClient;
        this.clock = clock;
        this.cancellationMinHours = cancellationMinHours;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservationDetail> findDetailById(Long id) {
        return repository.findById(id)
                .map(reservation -> new ReservationDetail(reservation, scheduleOf(reservation)));
    }
  
    @Override
    @Transactional
    public Reservation create(Reservation request) {
        if (request.getUserId() == null || request.getScheduleId() == null) {
            throw new BadRequestException("El usuario y el horario son obligatorios");
        }
        // courts-service bloquea el horario de forma atomica: si dos usuarios lo piden a la vez,
        // solo uno recibe el hold y el otro un 409.
        Schedule schedule;
        try {
            schedule = courtClient.hold(request.getScheduleId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("El horario no existe");
        } catch (FeignException.Conflict e) {
            throw new ConflictException("El horario ya no esta disponible");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setScheduleId(schedule.getId());
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setCreatedAt(LocalDateTime.now(clock));
        reservation.setHoldExpiresAt(schedule.getHeldUntil());
        reservation.setTotalAmount(schedule.getPrice());
        try {
            Reservation saved = repository.save(reservation);
            log.info("Reserva {} creada en hold para el horario {} (usuario {})",
                    saved.getId(), schedule.getId(), saved.getUserId());
            return saved;
        } catch (RuntimeException e) {
            // Compensacion: si no se pudo guardar la reserva, se libera el horario.
            releaseQuietly(schedule.getId());
            throw e;
        }
    }

    // Lo llamara payments-service cuando la pasarela apruebe el pago.
    @Override
    @Transactional(noRollbackFor = ConflictException.class) // el paso a EXPIRED debe quedar guardado
    public Reservation confirm(Long id) {
        Reservation reservation = findReservation(id);
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new ConflictException("La reserva no esta pendiente de pago");
        }
        if (!reservation.getHoldExpiresAt().isAfter(LocalDateTime.now(clock))) {
            repository.changeStatus(id, ReservationStatus.PENDING_PAYMENT, ReservationStatus.EXPIRED);
            throw new ConflictException("El tiempo para pagar la reserva expiro");
        }
        try {
            courtClient.reserve(reservation.getScheduleId());
        } catch (FeignException.Conflict e) {
            repository.changeStatus(id, ReservationStatus.PENDING_PAYMENT, ReservationStatus.EXPIRED);
            throw new ConflictException("El tiempo para pagar la reserva expiro");
        }
        if (repository.changeStatus(id, ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED) == 0) {
            // Otro proceso movio la reserva mientras tanto: se deshace el reserve en courts.
            releaseQuietly(reservation.getScheduleId());
            throw new ConflictException("La reserva ya no esta pendiente de pago");
        }
        log.info("Reserva {} confirmada", id);
        return findReservation(id);
    }

    @Override
    @Transactional
    public Reservation cancel(Long id) {
        Reservation reservation = findReservation(id);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            Schedule schedule = scheduleOf(reservation);
            LocalDateTime limit = schedule.startDateTime().minusHours(cancellationMinHours);
            if (LocalDateTime.now(clock).isAfter(limit)) {
                throw new ConflictException("Solo se puede cancelar hasta " + cancellationMinHours
                        + " horas antes del inicio de la reserva");
            }
        }
        return doCancel(reservation);
    }

    // El administrador puede cancelar sin la restriccion de la politica (ej. mantenimiento).
    @Override
    @Transactional
    public Reservation cancelByAdmin(Long id) {
        return doCancel(findReservation(id));
    }

    @Override
    @Transactional
    public int expirePendingHolds() {
        // Solo se marca la reserva: courts-service libera por su cuenta los horarios con hold vencido.
        int expired = 0;
        for (Reservation reservation : repository.findByStatusAndHoldExpiresAtLessThanEqual(
                ReservationStatus.PENDING_PAYMENT, LocalDateTime.now(clock))) {
            expired += repository.changeStatus(reservation.getId(),
                    ReservationStatus.PENDING_PAYMENT, ReservationStatus.EXPIRED);
        }
        return expired;
    }

    private Reservation doCancel(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        if (status == ReservationStatus.CONFIRMED) {
            // Primero se libera el horario: si courts-service no responde, la reserva queda igual
            // (confirmada) en vez de cancelada con el horario bloqueado para siempre.
            courtClient.release(reservation.getScheduleId());
        } else if (status == ReservationStatus.PENDING_PAYMENT) {
            // Si courts-service no responde, el hold igual vence solo.
            releaseQuietly(reservation.getScheduleId());
        } else {
            throw new ConflictException("La reserva ya esta cancelada o expirada");
        }
        if (repository.changeStatus(reservation.getId(), status, ReservationStatus.CANCELLED) == 0) {
            throw new ConflictException("La reserva cambio de estado, intenta de nuevo");
        }
        log.info("Reserva {} cancelada (estaba {})", reservation.getId(), status);
        return findReservation(reservation.getId());
    }

    private Schedule scheduleOf(Reservation reservation) {
        try {
            return courtClient.scheduleDetails(reservation.getScheduleId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("El horario de la reserva no existe");
        }
    }

    private void releaseQuietly(Long scheduleId) {
        try {
            courtClient.release(scheduleId);
        } catch (FeignException e) {
            log.warn("No se pudo liberar el horario {}; se liberara al vencer el hold: {}",
                    scheduleId, e.getMessage());
        }
    }

    private Reservation findReservation(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("La reserva no existe"));
    }

}
