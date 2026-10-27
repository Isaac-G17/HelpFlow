package com.helpflow.ticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.ticket.domain.model.TicketFilter;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Traduce el TicketFilter del dominio a una consulta dinámica (solo agrega los filtros no nulos). */
public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<TicketEntity> from(TicketFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.requesterId() != null) {
                predicates.add(cb.equal(root.get("requester").get("id"), filter.requesterId()));
            }
            if (filter.technicianId() != null) {
                predicates.add(cb.equal(root.get("technician").get("id"), filter.technicianId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
