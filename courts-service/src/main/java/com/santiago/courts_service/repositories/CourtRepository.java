package com.santiago.courts_service.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.santiago.courts_service.entities.Court;

public interface CourtRepository extends CrudRepository<Court, Long> {

    List<Court> findByLocationContainingIgnoreCase(String location);

    List<Court> findByNameContainingIgnoreCase(String name);

    List<Court> findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(String name, String location);
}
