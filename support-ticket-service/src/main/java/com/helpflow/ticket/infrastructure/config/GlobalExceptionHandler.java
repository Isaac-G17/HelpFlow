package com.helpflow.ticket.infrastructure.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.DuplicateResourceException;
import com.helpflow.ticket.domain.exception.ForbiddenOperationException;
import com.helpflow.ticket.domain.exception.InvalidCredentialsException;
import com.helpflow.ticket.domain.exception.InvalidStateTransitionException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.exception.TechnicianCapacityExceededException;
import com.helpflow.ticket.domain.exception.TechnicianDispatchUnavailableException;
import com.helpflow.ticket.infrastructure.security.SecurityMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce TODAS las excepciones a ProblemDetail (RFC 7807) con timestamp y traceId, y cuenta cada error en
 * helpflow.errors{status, exception}: así se distingue un 500 (BD / código) de un 503 (servicio externo).
 * Extiende ResponseEntityExceptionHandler para cubrir también los errores propios de Spring MVC (404, 405, 415...).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MeterRegistry meterRegistry;
    private final SecurityMetrics securityMetrics;

    // ---------------- 400 ----------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation", "Datos inválidos",
                "La solicitud tiene campos inválidos", servletRequest(request));
        problem.setProperty("errors", errors);
        return record(problem, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        String detail = "El cuerpo de la solicitud no es un JSON válido";
        if (ex.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            detail = "Valor '" + ife.getValue() + "' inválido. Valores permitidos: "
                    + Arrays.toString(ife.getTargetType().getEnumConstants());
        }
        return record(problem(HttpStatus.BAD_REQUEST, "malformed-request", "Solicitud mal formada", detail,
                servletRequest(request)), ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation", "Datos inválidos",
                "La solicitud tiene parámetros inválidos", request);
        problem.setProperty("errors", errors);
        return record(problem, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.BAD_REQUEST, "type-mismatch", "Parámetro inválido",
                "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue(), request), ex);
    }

    // ---------------- 401 / 403 ----------------

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        securityMetrics.authenticationFailure("bad_credentials");
        log.warn("Login fallido desde {}", request.getRemoteAddr());
        return record(problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", "No autenticado", ex.getMessage(),
                request), ex);
    }

    /** @PreAuthorize rechazado (rol sin permiso). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        securityMetrics.accessDenied("role");
        log.warn("403 {} {}: rol sin permiso", request.getMethod(), request.getRequestURI());
        return record(problem(HttpStatus.FORBIDDEN, "forbidden", "Acceso denegado",
                "Su rol no tiene permiso para esta operación", request), ex);
    }

    /** Recurso ajeno o transición no permitida para su rol (lo decide el caso de uso / dominio). */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Object> handleForbidden(ForbiddenOperationException ex, HttpServletRequest request) {
        securityMetrics.accessDenied("ownership");
        log.warn("403 {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return record(problem(HttpStatus.FORBIDDEN, "forbidden", "Acceso denegado", ex.getMessage(), request), ex);
    }

    // ---------------- 404 ----------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.NOT_FOUND, "not-found", "Recurso no encontrado", ex.getMessage(), request), ex);
    }

    // ---------------- 409 ----------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.CONFLICT, "duplicate", "Recurso duplicado", ex.getMessage(), request), ex);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<Object> handleTransition(InvalidStateTransitionException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.CONFLICT, "invalid-transition", "Transición de estado no permitida",
                ex.getMessage(), request), ex);
    }

    @ExceptionHandler(TechnicianCapacityExceededException.class)
    public ResponseEntity<Object> handleCapacity(TechnicianCapacityExceededException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.CONFLICT, "technician-capacity", "Técnico sin capacidad", ex.getMessage(),
                request), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return record(problem(HttpStatus.CONFLICT, "data-integrity", "Conflicto de datos",
                "La operación viola una restricción de datos (duplicado o referencia inválida)", request), ex);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                       HttpServletRequest request) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return record(problem(HttpStatus.CONFLICT, "concurrent-modification", "Modificación concurrente",
                "El recurso fue modificado por otra operación al mismo tiempo. Intente de nuevo.", request), ex);
    }

    // ---------------- 422 ----------------

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return record(problem(HttpStatus.UNPROCESSABLE_ENTITY, "business-rule", "Regla de negocio violada",
                ex.getMessage(), request), ex);
    }

    // ---------------- 501 (solo mientras haya TODO pendientes) ----------------

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Object> handleTodo(UnsupportedOperationException ex, HttpServletRequest request) {
        log.warn("Funcionalidad pendiente: {}", ex.getMessage());
        return record(problem(HttpStatus.NOT_IMPLEMENTED, "not-implemented", "Funcionalidad pendiente",
                ex.getMessage(), request), ex);
    }

    // ---------------- 503 ----------------

    @ExceptionHandler(TechnicianDispatchUnavailableException.class)
    public ResponseEntity<Object> handleDispatchUnavailable(TechnicianDispatchUnavailableException ex,
                                                            HttpServletRequest request) {
        return record(problem(HttpStatus.SERVICE_UNAVAILABLE, "dispatch-unavailable", "Servicio externo no disponible",
                ex.getMessage(), request), ex);
    }

    // ---------------- 500 ----------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return record(problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal", "Error interno",
                "Ocurrió un error inesperado. Use el traceId para rastrearlo en los logs.", request), ex);
    }

    // ---------------- Errores propios de Spring MVC (404 ruta, 405, 415...) ----------------

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ProblemDetail problem = body instanceof ProblemDetail pd ? pd : ProblemDetail.forStatus(statusCode);
        enrich(problem, servletRequest(request));
        return record(problem, ex);
    }

    // ---------------- helpers ----------------

    private ProblemDetail problem(HttpStatus status, String slug, String title, String detail,
                                 HttpServletRequest request) {
        return ProblemDetailFactory.create(status, slug, title, detail, request);
    }

    private void enrich(ProblemDetail problem, HttpServletRequest request) {
        if (problem.getInstance() == null && request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("traceId", TraceIdFilter.currentTraceId());
    }

    private ResponseEntity<Object> record(ProblemDetail problem, Exception ex) {
        Counter.builder("helpflow.errors")
                .description("Errores devueltos por la API")
                .tag("status", String.valueOf(problem.getStatus()))
                .tag("exception", ex.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return request instanceof ServletWebRequest swr ? swr.getRequest() : null;
    }
}
