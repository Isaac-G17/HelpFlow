package com.helpflow.ticket.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.ticket.domain.model.Assignment;
import com.helpflow.ticket.domain.model.SlaPolicy;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.TicketSummary;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.AssignmentEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.SlaPolicyEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import com.helpflow.ticket.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Dominio <-> entidades JPA. Las relaciones se ignoran en dominio -> entidad:
 * los adapters las asignan con getReferenceById (sin SELECT extra).
 * Leer "requester.id" de un proxy LAZY no lo inicializa (Hibernate ya conoce el id).
 */
@Mapper
public interface PersistenceMapper {

    // ---------- User ----------
    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);

    // ---------- Technician ----------
    @Mapping(target = "userId", source = "user.id")
    Technician toDomain(TechnicianEntity entity);

    @Mapping(target = "user", ignore = true)
    TechnicianEntity toEntity(Technician technician);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(Technician technician, @MappingTarget TechnicianEntity entity);

    // ---------- Ticket ----------
    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "technicianId", source = "technician.id")
    Ticket toDomain(TicketEntity entity);

    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "histories", ignore = true)
    TicketEntity toEntity(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "histories", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(Ticket ticket, @MappingTarget TicketEntity entity);

    @Mapping(target = "requesterId", source = "requester.id")
    @Mapping(target = "requesterName", source = "requester.name")
    @Mapping(target = "technicianId", source = "technician.id")
    @Mapping(target = "technicianCode", source = "technician.code")
    TicketSummary toSummary(TicketEntity entity);

    // ---------- TicketHistory ----------
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "performedBy", source = "performedBy.id")
    TicketHistory toDomain(TicketHistoryEntity entity);

    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    TicketHistoryEntity toEntity(TicketHistory history);

    // ---------- Assignment ----------
    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "technicianId", source = "technician.id")
    Assignment toDomain(AssignmentEntity entity);

    @Mapping(target = "ticket", ignore = true)
    @Mapping(target = "technician", ignore = true)
    AssignmentEntity toEntity(Assignment assignment);

    // ---------- SlaPolicy ----------
    SlaPolicy toDomain(SlaPolicyEntity entity);
}
