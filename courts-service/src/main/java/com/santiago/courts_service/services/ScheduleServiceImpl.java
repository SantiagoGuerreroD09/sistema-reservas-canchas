package com.santiago.courts_service.services;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santiago.courts_service.entities.Court;
import com.santiago.courts_service.entities.CourtStatus;
import com.santiago.courts_service.entities.Schedule;
import com.santiago.courts_service.entities.ScheduleStatus;
import com.santiago.courts_service.exceptions.BadRequestException;
import com.santiago.courts_service.exceptions.ConflictException;
import com.santiago.courts_service.exceptions.NotFoundException;
import com.santiago.courts_service.repositories.CourtRepository;
import com.santiago.courts_service.repositories.ScheduleRepository;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    final private ScheduleRepository repository;

    final private CourtRepository courtRepository;

    final private Clock clock;

    final private long holdMinutes;

    final private Environment environment;

    public ScheduleServiceImpl(ScheduleRepository repository, CourtRepository courtRepository, Clock clock,
            @Value("${config.hold.minutes}") long holdMinutes, Environment environment) {
        this.repository = repository;
        this.courtRepository = courtRepository;
        this.clock = clock;
        this.holdMinutes = holdMinutes;
        this.environment = environment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findByCourt(Long courtId, LocalDate date) {
        findCourt(courtId);
        if (date == null) {
            return repository.findByCourt_IdOrderByDateAscStartTimeAsc(courtId).stream().map(this::withPort).toList();
        }
        return repository.findByCourt_IdAndDateOrderByStartTimeAsc(courtId, date).stream().map(this::withPort).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Schedule> findAvailable(Long courtId, LocalDate date) {
        Court court = findCourt(courtId);
        if (court.getStatus() != CourtStatus.ACTIVE) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return repository.findByCourt_IdAndDateAndStatusOrderByStartTimeAsc(courtId, date, ScheduleStatus.AVAILABLE)
                .stream()
                .filter(schedule -> startOf(schedule).isAfter(now))
                .map(this::withPort)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Schedule> findById(Long id) {
        return repository.findById(id).map(this::withPort);
    }

    @Override
    @Transactional
    public Schedule save(Long courtId, Schedule schedule) {
        Court court = findCourt(courtId);
        validate(courtId, schedule, -1L);
        schedule.setId(null);
        schedule.setCourt(court);
        schedule.setStatus(ScheduleStatus.AVAILABLE);
        schedule.setHeldUntil(null);
        return withPort(repository.save(schedule));
    }

    @Override
    @Transactional
    public Optional<Schedule> update(Long id, Schedule schedule) {
        return repository.findById(id).map(scheduleDb -> {
            requireAvailable(scheduleDb, "editar");
            validate(scheduleDb.getCourtId(), schedule, id);
            scheduleDb.setDate(schedule.getDate());
            scheduleDb.setStartTime(schedule.getStartTime());
            scheduleDb.setEndTime(schedule.getEndTime());
            scheduleDb.setPrice(schedule.getPrice());
            return withPort(repository.save(scheduleDb));
        });
    }

    @Override
    @Transactional
    public Optional<Schedule> delete(Long id) {
        return repository.findById(id).map(schedule -> {
            requireAvailable(schedule, "eliminar");
            repository.delete(schedule);
            return schedule;
        });
    }

    @Override
    @Transactional
    public Schedule hold(Long id) {
        Schedule schedule = findSchedule(id);
        if (schedule.getCourt().getStatus() != CourtStatus.ACTIVE) {
            throw new ConflictException("La cancha no esta disponible para reservas");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!startOf(schedule).isAfter(now)) {
            throw new ConflictException("El horario ya paso");
        }
        int updated = repository.hold(id, now.plusMinutes(holdMinutes), ScheduleStatus.HELD, ScheduleStatus.AVAILABLE);
        if (updated == 0) {
            throw new ConflictException("El horario ya no esta disponible");
        }
        return withPort(findSchedule(id));
    }

    @Override
    @Transactional
    public Schedule reserve(Long id) {
        findSchedule(id);
        int updated = repository.reserve(id, LocalDateTime.now(clock), ScheduleStatus.RESERVED, ScheduleStatus.HELD);
        if (updated == 0) {
            throw new ConflictException("El bloqueo del horario expiro o el horario no estaba bloqueado");
        }
        return withPort(findSchedule(id));
    }

    @Override
    @Transactional
    public Schedule release(Long id) {
        findSchedule(id);
        // Idempotente: si ya estaba disponible no es un error.
        repository.release(id, ScheduleStatus.AVAILABLE, List.of(ScheduleStatus.HELD, ScheduleStatus.RESERVED));
        return withPort(findSchedule(id));
    }

    @Override
    @Transactional
    public int releaseExpiredHolds() {
        return repository.releaseExpiredHolds(LocalDateTime.now(clock), ScheduleStatus.AVAILABLE, ScheduleStatus.HELD);
    }

    private Court findCourt(Long courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new NotFoundException("La cancha no existe"));
    }

    private Schedule findSchedule(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("El horario no existe"));
    }

    private void requireAvailable(Schedule schedule, String action) {
        if (schedule.getStatus() != ScheduleStatus.AVAILABLE) {
            throw new ConflictException("No se puede " + action + " un horario reservado o en proceso de pago");
        }
    }

    private void validate(Long courtId, Schedule schedule, Long excludeId) {
        if (schedule.getDate() == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new BadRequestException("La fecha, la hora de inicio y la hora de fin son obligatorias");
        }
        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new BadRequestException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (schedule.getDate().isBefore(LocalDate.now(clock))) {
            throw new BadRequestException("No se pueden crear horarios en fechas pasadas");
        }
        if (schedule.getPrice() == null || schedule.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El precio debe ser mayor que cero");
        }
        if (repository.existsOverlap(courtId, schedule.getDate(), schedule.getStartTime(),
                schedule.getEndTime(), excludeId)) {
            throw new ConflictException("El horario se cruza con otro horario de la misma cancha");
        }
    }

    // Igual que en el curso: el puerto de esta instancia, para ver cual respondio.
    private Schedule withPort(Schedule schedule) {
        schedule.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
        return schedule;
    }

    private LocalDateTime startOf(Schedule schedule) {
        return LocalDateTime.of(schedule.getDate(), schedule.getStartTime());
    }

}
