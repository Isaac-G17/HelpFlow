package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.Technician;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepositoryPort {

    Technician save(Technician technician);

    Optional<Technician> findById(Long id);

    Optional<Technician> findByCode(String code);

    Optional<Technician> findByUserId(Long userId);

    List<Technician> findAll();

    /**
     * Técnicos ACTIVOS que pueden atender la categoría (si es OTRO, todos), ordenados por
     * menor carga. Base para elegir un técnico de respaldo (fallback).
     */
    List<Technician> findActiveCandidates(Category category);

    boolean existsByCode(String code);

    long count();
}
