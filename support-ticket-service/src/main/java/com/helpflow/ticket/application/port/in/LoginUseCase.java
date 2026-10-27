package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.enums.Role;

public interface LoginUseCase {

    LoginResult login(LoginCommand command);

    record LoginCommand(String email, String password) {
    }

    record LoginResult(String token, long expiresInSeconds, Role role) {
    }
}
