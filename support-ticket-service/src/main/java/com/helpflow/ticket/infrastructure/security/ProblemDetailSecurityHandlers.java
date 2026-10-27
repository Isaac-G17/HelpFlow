package com.helpflow.ticket.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpflow.ticket.infrastructure.config.ProblemDetailFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 401 y 403 generados por los filtros de Spring Security, también en formato ProblemDetail. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemDetailSecurityHandlers {

    private final ObjectMapper objectMapper;
    private final SecurityMetrics securityMetrics;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            Object jwtError = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
            securityMetrics.authenticationFailure(jwtError != null ? "invalid_token" : "missing_token");
            String detail = jwtError != null ? jwtError.toString() : "Se requiere autenticación (token Bearer)";
            log.warn("401 {} {}: {}", request.getMethod(), request.getRequestURI(), detail);
            write(response, request, HttpStatus.UNAUTHORIZED, "unauthorized", "No autenticado", detail);
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            securityMetrics.accessDenied("role");
            log.warn("403 {} {}: rol sin permiso", request.getMethod(), request.getRequestURI());
            write(response, request, HttpStatus.FORBIDDEN, "forbidden", "Acceso denegado",
                    "Su rol no tiene permiso para este recurso");
        };
    }

    private void write(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String slug,
                       String title, String detail) throws IOException {
        ProblemDetail problem = ProblemDetailFactory.create(status, slug, title, detail, request);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
