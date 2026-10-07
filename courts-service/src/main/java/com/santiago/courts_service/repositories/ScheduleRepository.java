package com.santiago.courts_service.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.santiago.courts_service.entities.Schedule;
import com.santiago.courts_service.entities.ScheduleStatus;

public interface ScheduleRepository extends CrudRepository<Schedule, Long> {

    List<Schedule> findByCourt_IdOrderByDateAscStartTimeAsc(Long courtId);

    List<Schedule> findByCourt_IdAndDateOrderByStartTimeAsc(Long courtId, LocalDate date);

    List<Schedule> findByCourt_IdAndDateAndStatusOrderByStartTimeAsc(Long courtId, LocalDate date, ScheduleStatus status);

    boolean existsByCourt_IdAndStatusIn(Long courtId, Collection<ScheduleStatus> statuses);

    @Query("""
            select count(s) > 0 from Schedule s
            where s.court.id = :courtId and s.date = :date
              and s.startTime < :endTime and s.endTime > :startTime
              and s.id <> :excludeId
            """)
    boolean existsOverlap(@Param("courtId") Long courtId, @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    // Las transiciones de estado son UPDATE condicionales: la base de datos garantiza que
    // solo una peticion concurrente gane el horario (sin doble reserva). Devuelven filas afectadas.

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Schedule s set s.status = :held, s.heldUntil = :heldUntil
            where s.id = :id and s.status = :available
            """)
    int hold(@Param("id") Long id, @Param("heldUntil") LocalDateTime heldUntil,
            @Param("held") ScheduleStatus held, @Param("available") ScheduleStatus available);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Schedule s set s.status = :reserved, s.heldUntil = null
            where s.id = :id and s.status = :held and s.heldUntil > :now
            """)
    int reserve(@Param("id") Long id, @Param("now") LocalDateTime now,
            @Param("reserved") ScheduleStatus reserved, @Param("held") ScheduleStatus held);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Schedule s set s.status = :available, s.heldUntil = null
            where s.id = :id and s.status in :from
            """)
    int release(@Param("id") Long id, @Param("available") ScheduleStatus available,
            @Param("from") Collection<ScheduleStatus> from);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Schedule s set s.status = :available, s.heldUntil = null
            where s.status = :held and s.heldUntil <= :now
            """)
    int releaseExpiredHolds(@Param("now") LocalDateTime now,
            @Param("available") ScheduleStatus available, @Param("held") ScheduleStatus held);

    @Modifying
    @Query("delete from Schedule s where s.court.id = :courtId")
    void deleteByCourtId(@Param("courtId") Long courtId);
}
