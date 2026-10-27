package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.model.enums.Role;

/** Usuario autenticado (lo construye el filtro JWT y llega a los casos de uso). */
public record CurrentUser(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isTechnician() {
        return role == Role.TECNICO;
    }

    public boolean isUser() {
        return role == Role.USUARIO;
    }
}
