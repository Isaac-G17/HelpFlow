package com.helpflow.ticket.infrastructure.adapter.out.persistence.entity;

import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario 1–N Tickets. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(nullable = false, length = 60)
    private String sede;

    @Column(name = "affected_asset", nullable = false, length = 120)
    private String affectedAsset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TicketStatus status;

    /** Ticket N–1 Técnico asignado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private TechnicianEntity technician;

    @Column(nullable = false)
    private boolean escalated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sla_deadline", nullable = false)
    private Instant slaDeadline;

    @Column(name = "first_response_at")
    private Instant firstResponseAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Version
    private Long version;

    /** Ticket 1–N Historiales (lado inverso; el historial se guarda por su propio repositorio). */
    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<TicketHistoryEntity> histories = new ArrayList<>();
}
