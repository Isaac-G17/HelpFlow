package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.User;

public interface RegisterUserUseCase {

    /** Registra un usuario nuevo con el rol básico USUARIO. */
    User register(RegisterCommand command);

    record RegisterCommand(String document, String name, String email, String password, String sede, String area) {
    }
}
