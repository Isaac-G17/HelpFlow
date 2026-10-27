package com.helpflow.ticket.domain.exception;

/** 401: mismo mensaje si el correo no existe o la contraseña es incorrecta (no revela qué falló). */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
