package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyJpaRepository extends JpaRepository<SlaPolicyEntity, Priority> {
}
