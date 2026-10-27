package com.helpflow.ticket.infrastructure.adapter.out.persistence.entity;

import com.helpflow.ticket.domain.model.AccountStatus;
import com.helpflow.ticket.domain.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
public class TechnicianEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category specialty;

    @Column(nullable = false, length = 60)
    private String sede;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status;

    @Column(name = "max_active_tickets", nullable = false)
    private int maxActiveTickets;

    @Column(name = "active_tickets", nullable = false)
    private int activeTickets;

    /** Bloqueo optimista: dos asignaciones concurrentes al mismo técnico -> la segunda falla (409). */
    @Version
    private Long version;
}
