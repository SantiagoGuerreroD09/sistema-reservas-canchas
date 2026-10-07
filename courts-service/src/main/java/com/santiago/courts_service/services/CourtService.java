package com.santiago.courts_service.services;

import java.util.List;
import java.util.Optional;

import com.santiago.courts_service.entities.Court;

public interface CourtService {

    List<Court> findAll(String name, String location);

    Optional<Court> findById(Long id);

    Court save(Court court);

    Optional<Court> update(Long id, Court court);

    Optional<Court> delete(Long id);
}
