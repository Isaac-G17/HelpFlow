package com.helpflow.ticket.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/** Construye los ProblemDetail (RFC 7807) con los campos exigidos: type, title, status, detail, instance, timestamp, traceId. */
public final class ProblemDetailFactory {

    private static final String TYPE_BASE = "https://helpflow.com/errors/";

    private ProblemDetailFactory() {
    }

    public static ProblemDetail create(HttpStatus status, String slug, String title, String detail,
                                       HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_BASE + slug));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", TraceIdFilter.currentTraceId());
        return problem;
    }
}
