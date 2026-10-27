package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * El dominio Technician expone documento y nombre del usuario vinculado: todas las lecturas traen
 * el usuario en la misma consulta (@EntityGraph) para no disparar una consulta extra por técnico (N+1).
 */
public interface TechnicianJpaRepository extends JpaRepository<TechnicianEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<TechnicianEntity> findById(Long id);

    @EntityGraph(attributePaths = "user")
    Optional<TechnicianEntity> findByCode(String code);

    @EntityGraph(attributePaths = "user")
    Optional<TechnicianEntity> findByUser_Id(Long userId);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = "user")
    List<TechnicianEntity> findByStatusAndSpecialtyOrderByActiveTicketsAscIdAsc(AccountStatus status, Category specialty);

    @EntityGraph(attributePaths = "user")
    List<TechnicianEntity> findByStatusOrderByActiveTicketsAscIdAsc(AccountStatus status);

    @EntityGraph(attributePaths = "user")
    List<TechnicianEntity> findAllByOrderByCodeAsc();
}
