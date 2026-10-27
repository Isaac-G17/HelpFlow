package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.domain.model.AccountStatus;
import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicianJpaRepository extends JpaRepository<TechnicianEntity, Long> {

    Optional<TechnicianEntity> findByCode(String code);

    Optional<TechnicianEntity> findByUser_Id(Long userId);

    boolean existsByCode(String code);

    List<TechnicianEntity> findByStatusAndSpecialtyOrderByActiveTicketsAscIdAsc(AccountStatus status, Category specialty);

    List<TechnicianEntity> findByStatusOrderByActiveTicketsAscIdAsc(AccountStatus status);

    List<TechnicianEntity> findAllByOrderByCodeAsc();
}
