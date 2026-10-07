package com.santiago.courts_service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.santiago.courts_service.entities.Court;
import com.santiago.courts_service.entities.CourtStatus;
import com.santiago.courts_service.entities.ScheduleStatus;
import com.santiago.courts_service.exceptions.BadRequestException;
import com.santiago.courts_service.exceptions.ConflictException;
import com.santiago.courts_service.repositories.CourtRepository;
import com.santiago.courts_service.repositories.ReviewRepository;
import com.santiago.courts_service.repositories.ScheduleRepository;

@Service
public class CourtServiceImpl implements CourtService {

    final private CourtRepository repository;

    final private ScheduleRepository scheduleRepository;

    final private ReviewRepository reviewRepository;

    public CourtServiceImpl(CourtRepository repository, ScheduleRepository scheduleRepository,
            ReviewRepository reviewRepository) {
        this.repository = repository;
        this.scheduleRepository = scheduleRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Court> findAll(String name, String location) {
        boolean byName = name != null && !name.isBlank();
        boolean byLocation = location != null && !location.isBlank();
        if (byName && byLocation) {
            return repository.findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(name, location);
        }
        if (byName) {
            return repository.findByNameContainingIgnoreCase(name);
        }
        if (byLocation) {
            return repository.findByLocationContainingIgnoreCase(location);
        }
        return (List<Court>) repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Court> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Court save(Court court) {
        validate(court);
        court.setId(null);
        if (court.getStatus() == null) {
            court.setStatus(CourtStatus.ACTIVE);
        }
        return repository.save(court);
    }

    @Override
    @Transactional
    public Optional<Court> update(Long id, Court court) {
        validate(court);
        return repository.findById(id).map(courtDb -> {
            courtDb.setName(court.getName());
            courtDb.setLocation(court.getLocation());
            courtDb.setDescription(court.getDescription());
            if (court.getStatus() != null) {
                courtDb.setStatus(court.getStatus());
            }
            return repository.save(courtDb);
        });
    }

    @Override
    @Transactional
    public Optional<Court> delete(Long id) {
        return repository.findById(id).map(court -> {
            if (scheduleRepository.existsByCourt_IdAndStatusIn(id,
                    List.of(ScheduleStatus.HELD, ScheduleStatus.RESERVED))) {
                throw new ConflictException(
                        "La cancha tiene horarios reservados o en proceso de pago; cancelalos o desactiva la cancha");
            }
            reviewRepository.deleteByCourtId(id);
            scheduleRepository.deleteByCourtId(id);
            repository.delete(court);
            return court;
        });
    }

    private void validate(Court court) {
        if (court.getName() == null || court.getName().isBlank()) {
            throw new BadRequestException("El nombre de la cancha es obligatorio");
        }
        if (court.getLocation() == null || court.getLocation().isBlank()) {
            throw new BadRequestException("La ubicacion de la cancha es obligatoria");
        }
    }

}
