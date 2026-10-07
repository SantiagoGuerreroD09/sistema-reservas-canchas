package com.santiago.courts_service.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.santiago.courts_service.entities.Schedule;

public interface ScheduleService {

    List<Schedule> findByCourt(Long courtId, LocalDate date);

    List<Schedule> findAvailable(Long courtId, LocalDate date);

    Optional<Schedule> findById(Long id);

    Schedule save(Long courtId, Schedule schedule);

    Optional<Schedule> update(Long id, Schedule schedule);

    Optional<Schedule> delete(Long id);

    Schedule hold(Long id);

    Schedule reserve(Long id);

    Schedule release(Long id);

    int releaseExpiredHolds();
}
