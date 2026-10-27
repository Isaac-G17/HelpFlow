-- =====================================================================
-- V1: esquema base (usuarios, técnicos y tickets)
-- NUNCA editar esta migración una vez aplicada: los cambios van en V4, V5...
-- =====================================================================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    document      VARCHAR(20)  NOT NULL,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    sede          VARCHAR(60)  NOT NULL,
    area          VARCHAR(60)  NOT NULL,
    status        VARCHAR(10)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(10)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_document UNIQUE (document),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT ck_users_role CHECK (role IN ('USUARIO', 'TECNICO', 'ADMIN'))
);

CREATE TABLE technicians (
    id                 BIGSERIAL PRIMARY KEY,
    code               VARCHAR(10) NOT NULL,
    user_id            BIGINT      NOT NULL,
    specialty          VARCHAR(10) NOT NULL,
    sede               VARCHAR(60) NOT NULL,
    status             VARCHAR(10) NOT NULL,
    max_active_tickets INT         NOT NULL,
    active_tickets     INT         NOT NULL DEFAULT 0,
    version            BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_technicians_code UNIQUE (code),
    CONSTRAINT uk_technicians_user UNIQUE (user_id),
    CONSTRAINT fk_technicians_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_technicians_specialty CHECK (specialty IN ('HARDWARE', 'SOFTWARE', 'RED', 'ACCESOS', 'OTRO')),
    CONSTRAINT ck_technicians_status CHECK (status IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT ck_technicians_load CHECK (active_tickets >= 0 AND active_tickets <= max_active_tickets),
    CONSTRAINT ck_technicians_max CHECK (max_active_tickets > 0)
);

CREATE TABLE tickets (
    id                BIGSERIAL PRIMARY KEY,
    requester_id      BIGINT       NOT NULL,
    title             VARCHAR(150) NOT NULL,
    description       TEXT         NOT NULL,
    category          VARCHAR(10)  NOT NULL,
    priority          VARCHAR(10)  NOT NULL,
    sede              VARCHAR(60)  NOT NULL,
    affected_asset    VARCHAR(120) NOT NULL,
    status            VARCHAR(15)  NOT NULL,
    technician_id     BIGINT,
    escalated         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL,
    sla_deadline      TIMESTAMPTZ  NOT NULL,
    first_response_at TIMESTAMPTZ,
    resolved_at       TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_tickets_requester FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_technician FOREIGN KEY (technician_id) REFERENCES technicians (id),
    CONSTRAINT ck_tickets_category CHECK (category IN ('HARDWARE', 'SOFTWARE', 'RED', 'ACCESOS', 'OTRO')),
    CONSTRAINT ck_tickets_priority CHECK (priority IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT ck_tickets_status CHECK (status IN ('ABIERTO', 'ASIGNADO', 'EN_PROGRESO', 'RESUELTO', 'CERRADO', 'CANCELADO')),
    CONSTRAINT ck_tickets_sla CHECK (sla_deadline > created_at)
);

CREATE INDEX idx_tickets_requester ON tickets (requester_id);
CREATE INDEX idx_tickets_technician ON tickets (technician_id);
CREATE INDEX idx_tickets_status_deadline ON tickets (status, sla_deadline);
