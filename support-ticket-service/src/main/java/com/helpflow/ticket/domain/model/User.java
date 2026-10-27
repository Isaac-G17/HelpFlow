package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Role;

import java.time.Instant;
import java.util.Locale;

/**
 * Usuario de HelpFlow (solicitante, técnico o administrador).
 *
 * EJEMPLO RESUELTO: esta clase ya está implementada para que veas el patrón que debes seguir
 * en Ticket y Technician (fábrica que valida + métodos con reglas de negocio + sin frameworks).
 */
public class User {

    public static final String CORPORATE_DOMAIN = "@helpflow.com";

    private final Long id;
    private final String document;
    private String name;
    private final String email;
    private String sede;
    private String area;
    private AccountStatus status;
    private final String passwordHash;
    private final Role role;
    private final Instant createdAt;

    /** Constructor de reconstrucción (lo usa el mapper de persistencia). Para crear usa {@link #create}. */
    public User(Long id, String document, String name, String email, String sede, String area,
                AccountStatus status, String passwordHash, Role role, Instant createdAt) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.email = email;
        this.sede = sede;
        this.area = area;
        this.status = status;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User create(String document, String name, String email, String sede, String area,
                              String passwordHash, Role role, Instant now) {
        String normalizedEmail = DomainValidation.requireText(email, "email").toLowerCase(Locale.ROOT);
        if (!normalizedEmail.endsWith(CORPORATE_DOMAIN)) {
            throw new BusinessRuleException("El correo debe ser corporativo (" + CORPORATE_DOMAIN + ")");
        }
        return new User(
                null,
                DomainValidation.requireText(document, "document"),
                DomainValidation.requireText(name, "name"),
                normalizedEmail,
                DomainValidation.normalizeSede(sede),
                DomainValidation.requireText(area, "area"),
                AccountStatus.ACTIVO,
                DomainValidation.requireText(passwordHash, "passwordHash"),
                DomainValidation.requireNonNull(role, "role"),
                now
        );
    }

    /** Regla del enunciado: solo un usuario ACTIVO puede registrar tickets. */
    public void ensureActive() {
        if (!isActive()) {
            throw new InactiveUserException("El usuario " + email + " está INACTIVO y no puede operar");
        }
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVO;
    }

    public void updateProfile(String name, String sede, String area) {
        this.name = DomainValidation.requireText(name, "name");
        this.sede = DomainValidation.normalizeSede(sede);
        this.area = DomainValidation.requireText(area, "area");
    }

    public void changeStatus(AccountStatus newStatus) {
        this.status = DomainValidation.requireNonNull(newStatus, "status");
    }

    public Long getId() {
        return id;
    }

    public String getDocument() {
        return document;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSede() {
        return sede;
    }

    public String getArea() {
        return area;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
