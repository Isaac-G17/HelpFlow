package com.helpflow.ticket.application.port.out;

import com.helpflow.ticket.domain.model.User;

public interface TokenProviderPort {

    String generateToken(User user);

    long expirationSeconds();
}
