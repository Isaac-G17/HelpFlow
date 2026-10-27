package com.helpflow.ticket.infrastructure.security;

import com.helpflow.ticket.application.port.out.TokenProviderPort;
import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.Role;
import com.helpflow.ticket.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * Emite y valida JWT firmados con HMAC-SHA256. Stateless: el token lleva id, email y rol,
 * así el filtro no consulta la BD en cada petición.
 */
@Component
public class JwtTokenProvider implements TokenProviderPort {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationSeconds;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.expirationSeconds = properties.expirationSeconds();
        this.clock = clock;
    }

    @Override
    public String generateToken(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_UID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuer("helpflow")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    @Override
    public long expirationSeconds() {
        return expirationSeconds;
    }

    /** @throws JwtException si la firma no es válida, está mal formado o expiró. */
    public CurrentUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer("helpflow")
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new CurrentUser(
                claims.get(CLAIM_UID, Long.class),
                claims.getSubject(),
                Role.valueOf(claims.get(CLAIM_ROLE, String.class)));
    }
}
