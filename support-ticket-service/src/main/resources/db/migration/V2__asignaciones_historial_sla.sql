-- =====================================================================
-- V2: asignaciones, historial de tickets y políticas de SLA
-- =====================================================================

CREATE TABLE assignments (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT      NOT NULL,
    technician_id   BIGINT      NOT NULL,
    availability    VARCHAR(10),
    estimated_hours INT,
    origin          VARCHAR(10) NOT NULL,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    assigned_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_assignments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_assignments_technician FOREIGN KEY (technician_id) REFERENCES technicians (id),
    CONSTRAINT ck_assignments_origin CHECK (origin IN ('DISPATCH', 'FALLBACK', 'MANUAL')),
    CONSTRAINT ck_assignments_availability CHECK (availability IS NULL OR availability IN ('ALTA', 'MEDIA', 'BAJA'))
);

-- Un ticket solo puede tener UNA asignación activa a la vez.
CREATE UNIQUE INDEX uk_assignments_active_ticket ON assignments (ticket_id) WHERE active;
CREATE INDEX idx_assignments_technician ON assignments (technician_id);

CREATE TABLE ticket_history (
    id           BIGSERIAL PRIMARY KEY,
    ticket_id    BIGINT       NOT NULL,
    from_status  VARCHAR(15),
    to_status    VARCHAR(15)  NOT NULL,
    action       VARCHAR(20)  NOT NULL,
    observation  VARCHAR(500) NOT NULL,
    performed_by BIGINT,
    created_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_history_user FOREIGN KEY (performed_by) REFERENCES users (id),
    CONSTRAINT ck_history_action CHECK (action IN ('CREATED', 'ASSIGNED', 'STATUS_CHANGED', 'ESCALATED', 'REASSIGNED', 'DISPATCH_FAILED'))
);

CREATE INDEX idx_history_ticket ON ticket_history (ticket_id, created_at);

CREATE TABLE sla_policies (
    priority         VARCHAR(10) PRIMARY KEY,
    response_hours   INT         NOT NULL,
    resolution_hours INT         NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_sla_priority CHECK (priority IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT ck_sla_hours CHECK (response_hours > 0 AND resolution_hours > response_hours)
);

-- Políticas por defecto (configurables por ADMIN): horas para responder y para resolver.
INSERT INTO sla_policies (priority, response_hours, resolution_hours) VALUES
    ('CRITICA', 1, 4),
    ('ALTA', 2, 8),
    ('MEDIA', 4, 24),
    ('BAJA', 8, 72);
