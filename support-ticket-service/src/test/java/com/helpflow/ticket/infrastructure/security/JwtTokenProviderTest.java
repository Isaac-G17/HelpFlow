package com.helpflow.ticket.infrastructure.security;

import com.helpflow.ticket.domain.model.CurrentUser;
import com.helpflow.ticket.domain.model.TestData;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.domain.model.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Emisión y validación del JWT sin levantar Spring. */
class JwtTokenProviderTest {

    private static final String SECRET = "aGVscGZsb3ctZGV2LXNlY3JldC1rZXktY2FtYmlhbWUtZW4tcHJvZHVjY2lvbi0yMDI2";
    private static final String OTHER_SECRET = "b3RyYS1jbGF2ZS1kZS1wcnVlYmEtcXVlLW5vLWVzLWxhLWRlLWhlbHBmbG93LTIwMjY=";

    private static JwtTokenProvider providerAt(String secret, Instant now, long expirationSeconds) {
        return new JwtTokenProvider(new JwtProperties(secret, expirationSeconds), Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void elTokenLlevaIdCorreoYRol() {
        User ana = TestData.activeUser(1L);
        JwtTokenProvider provider = providerAt(SECRET, Instant.now(), 3600);

        CurrentUser parsed = provider.parse(provider.generateToken(ana));

        assertThat(parsed).isEqualTo(new CurrentUser(1L, "user1@helpflow.com", Role.USUARIO));
        assertThat(provider.expirationSeconds()).isEqualTo(3600);
    }

    @Test
    void unTokenExpiradoSeRechaza() {
        // Emitido hace 2 h con vigencia de 1 h: venció hace 1 h.
        String expired = providerAt(SECRET, Instant.now().minus(Duration.ofHours(2)), 3600)
                .generateToken(TestData.activeUser(1L));

        assertThatThrownBy(() -> providerAt(SECRET, Instant.now(), 3600).parse(expired))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        String foreign = providerAt(OTHER_SECRET, Instant.now(), 3600).generateToken(TestData.activeUser(1L));

        assertThatThrownBy(() -> providerAt(SECRET, Instant.now(), 3600).parse(foreign))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void unTokenAlteradoSeRechaza() {
        JwtTokenProvider provider = providerAt(SECRET, Instant.now(), 3600);
        String token = provider.generateToken(TestData.activeUser(1L));
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThatThrownBy(() -> provider.parse(tampered)).isInstanceOf(JwtException.class);
    }
}
