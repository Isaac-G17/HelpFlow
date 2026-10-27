package com.helpflow.ticket.infrastructure.security;

import com.helpflow.ticket.domain.model.CurrentUser;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer <token>", lo valida y deja un CurrentUser como principal.
 * Si el token es inválido no corta la petición: simplemente no autentica, y Spring Security
 * responde 401 (vía el AuthenticationEntryPoint) si el endpoint lo requiere.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    static final String JWT_ERROR_ATTRIBUTE = "helpflow.jwtError";
    private static final String BEARER = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            try {
                CurrentUser user = tokenProvider.parse(header.substring(BEARER.length()).trim());
                var authentication = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("JWT rechazado: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "Token inválido o expirado");
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
