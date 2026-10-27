-- =====================================================================
-- V3: datos iniciales para demo y pruebas manuales.
-- Contraseña de TODOS los usuarios semilla: Password123*  (hash BCrypt, coste 10)
-- =====================================================================

INSERT INTO users (document, name, email, sede, area, status, password_hash, role) VALUES
    ('1000000001', 'Admin HelpFlow',     'admin@helpflow.com', 'MEDELLIN', 'TI',          'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'ADMIN'),
    ('1000000101', 'Tecnico Hardware 1', 'tec01@helpflow.com', 'MEDELLIN', 'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000102', 'Tecnico Hardware 2', 'tec02@helpflow.com', 'BOGOTA',   'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000103', 'Tecnico Software 1', 'tec03@helpflow.com', 'MEDELLIN', 'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000104', 'Tecnico Software 2', 'tec04@helpflow.com', 'BOGOTA',   'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000105', 'Tecnico Red 1',      'tec05@helpflow.com', 'MEDELLIN', 'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000106', 'Tecnico Red 2',      'tec06@helpflow.com', 'BOGOTA',   'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000107', 'Tecnico Accesos 1',  'tec07@helpflow.com', 'MEDELLIN', 'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000108', 'Tecnico Accesos 2',  'tec08@helpflow.com', 'BOGOTA',   'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000109', 'Tecnico General 1',  'tec09@helpflow.com', 'MEDELLIN', 'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000110', 'Tecnico General 2',  'tec10@helpflow.com', 'BOGOTA',   'Soporte',     'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000111', 'Tecnico Hardware 3', 'tec11@helpflow.com', 'BARRANQUILLA', 'Soporte', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000112', 'Tecnico Software 3', 'tec12@helpflow.com', 'BARRANQUILLA', 'Soporte', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000113', 'Tecnico Red 3',      'tec13@helpflow.com', 'BARRANQUILLA', 'Soporte', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000114', 'Tecnico Accesos 3',  'tec14@helpflow.com', 'BARRANQUILLA', 'Soporte', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000115', 'Tecnico General 3',  'tec15@helpflow.com', 'BARRANQUILLA', 'Soporte', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'TECNICO'),
    ('1000000201', 'Ana Gomez',          'ana@helpflow.com',   'MEDELLIN', 'Contabilidad', 'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'USUARIO'),
    ('1000000202', 'Luis Perez',         'luis@helpflow.com',  'BOGOTA',   'Ventas',       'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'USUARIO'),
    ('1000000204', 'Maria Rodriguez',    'maria@helpflow.com', 'BARRANQUILLA', 'Compras',  'ACTIVO',   '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'USUARIO'),
    ('1000000203', 'Pedro Inactivo',     'pedro@helpflow.com', 'MEDELLIN', 'Logistica',    'INACTIVO', '$2a$10$IQ8yoAtC4uQTtrBLw7DCvOd46Oo7703KSinxUX9SSyjAyUnG9ry6a', 'USUARIO');

-- 15 técnicos: 3 por categoría, uno por sede (MEDELLIN, BOGOTA, BARRANQUILLA).
-- Coinciden con el catálogo de technician-dispatch-service.
INSERT INTO technicians (code, user_id, salary, affiliation_date, specialty, sede, status, max_active_tickets, active_tickets)
SELECT t.code, u.id, t.salary, t.affiliation_date, t.specialty, t.sede, 'ACTIVO', 5, 0
FROM (VALUES
    ('TEC-001', 'tec01@helpflow.com', 3200000.00, DATE '2021-03-15', 'HARDWARE', 'MEDELLIN'),
    ('TEC-002', 'tec02@helpflow.com', 3100000.00, DATE '2022-07-01', 'HARDWARE', 'BOGOTA'),
    ('TEC-003', 'tec03@helpflow.com', 3800000.00, DATE '2020-01-20', 'SOFTWARE', 'MEDELLIN'),
    ('TEC-004', 'tec04@helpflow.com', 3650000.00, DATE '2023-02-10', 'SOFTWARE', 'BOGOTA'),
    ('TEC-005', 'tec05@helpflow.com', 4000000.00, DATE '2019-09-05', 'RED',      'MEDELLIN'),
    ('TEC-006', 'tec06@helpflow.com', 3900000.00, DATE '2021-11-22', 'RED',      'BOGOTA'),
    ('TEC-007', 'tec07@helpflow.com', 3500000.00, DATE '2022-04-18', 'ACCESOS',  'MEDELLIN'),
    ('TEC-008', 'tec08@helpflow.com', 3450000.00, DATE '2024-01-08', 'ACCESOS',  'BOGOTA'),
    ('TEC-009', 'tec09@helpflow.com', 2900000.00, DATE '2023-06-12', 'OTRO',     'MEDELLIN'),
    ('TEC-010', 'tec10@helpflow.com', 2850000.00, DATE '2024-08-26', 'OTRO',     'BOGOTA'),
    ('TEC-011', 'tec11@helpflow.com', 3000000.00, DATE '2022-10-03', 'HARDWARE', 'BARRANQUILLA'),
    ('TEC-012', 'tec12@helpflow.com', 3600000.00, DATE '2021-05-17', 'SOFTWARE', 'BARRANQUILLA'),
    ('TEC-013', 'tec13@helpflow.com', 3850000.00, DATE '2020-08-31', 'RED',      'BARRANQUILLA'),
    ('TEC-014', 'tec14@helpflow.com', 3400000.00, DATE '2023-09-14', 'ACCESOS',  'BARRANQUILLA'),
    ('TEC-015', 'tec15@helpflow.com', 2800000.00, DATE '2025-02-03', 'OTRO',     'BARRANQUILLA')
) AS t(code, email, salary, affiliation_date, specialty, sede)
JOIN users u ON u.email = t.email;

-- Ticket 1: VENCIDO (para evidenciar la métrica helpflow.tickets.overdue).
INSERT INTO tickets (requester_id, title, description, category, priority, sede, affected_asset, status,
                     technician_id, escalated, created_at, sla_deadline, version)
SELECT u.id, 'Sin conexión a la VPN', 'Desde ayer no conecta la VPN corporativa en el portátil.',
       'RED', 'ALTA', 'MEDELLIN', 'Portátil LT-0231', 'ASIGNADO',
       t.id, FALSE, now() - INTERVAL '10 hours', now() - INTERVAL '2 hours', 0
FROM users u, technicians t
WHERE u.email = 'ana@helpflow.com' AND t.code = 'TEC-005';

-- Ticket 2: EN_PROGRESO, dentro del SLA.
INSERT INTO tickets (requester_id, title, description, category, priority, sede, affected_asset, status,
                     technician_id, escalated, created_at, sla_deadline, first_response_at, version)
SELECT u.id, 'Excel se cierra al abrir macros', 'El libro de cierre contable se cierra al ejecutar macros.',
       'SOFTWARE', 'MEDIA', 'BOGOTA', 'PC-VEN-014', 'EN_PROGRESO',
       t.id, FALSE, now() - INTERVAL '3 hours', now() + INTERVAL '21 hours', now() - INTERVAL '2 hours', 0
FROM users u, technicians t
WHERE u.email = 'luis@helpflow.com' AND t.code = 'TEC-004';

-- Ticket 3: ABIERTO sin técnico (para probar auto-assign / reasignación manual).
INSERT INTO tickets (requester_id, title, description, category, priority, sede, affected_asset, status,
                     escalated, created_at, sla_deadline, version)
SELECT u.id, 'Solicitud de acceso a carpeta compartida', 'Necesito acceso de lectura a \\fs01\finanzas.',
       'ACCESOS', 'BAJA', 'MEDELLIN', 'Servidor FS01', 'ABIERTO',
       FALSE, now() - INTERVAL '1 hours', now() + INTERVAL '71 hours', 0
FROM users u
WHERE u.email = 'ana@helpflow.com';

-- Ticket 4: ABIERTO en BARRANQUILLA (para probar la asignación con sede distinta a Medellín/Bogotá).
INSERT INTO tickets (requester_id, title, description, category, priority, sede, affected_asset, status,
                     escalated, created_at, sla_deadline, version)
SELECT u.id, 'Portátil no enciende', 'El portátil no enciende ni con el cargador conectado.',
       'HARDWARE', 'MEDIA', 'BARRANQUILLA', 'LT-0518', 'ABIERTO',
       FALSE, now() - INTERVAL '30 minutes', now() + INTERVAL '1410 minutes', 0
FROM users u
WHERE u.email = 'maria@helpflow.com';

UPDATE technicians SET active_tickets = 1 WHERE code IN ('TEC-005', 'TEC-004');

INSERT INTO assignments (ticket_id, technician_id, availability, estimated_hours, origin, active, assigned_at)
SELECT tk.id, tk.technician_id, 'MEDIA', 4, 'DISPATCH', TRUE, tk.created_at + INTERVAL '1 minute'
FROM tickets tk
WHERE tk.technician_id IS NOT NULL;

INSERT INTO ticket_history (ticket_id, from_status, to_status, action, observation, performed_by, created_at)
SELECT tk.id, NULL, 'ABIERTO', 'CREATED', 'Ticket registrado', tk.requester_id, tk.created_at
FROM tickets tk;

INSERT INTO ticket_history (ticket_id, from_status, to_status, action, observation, performed_by, created_at)
SELECT tk.id, 'ABIERTO', 'ASIGNADO', 'ASSIGNED', 'Asignado por technician-dispatch-service', NULL,
       tk.created_at + INTERVAL '1 minute'
FROM tickets tk
WHERE tk.technician_id IS NOT NULL;

INSERT INTO ticket_history (ticket_id, from_status, to_status, action, observation, performed_by, created_at)
SELECT tk.id, 'ASIGNADO', 'EN_PROGRESO', 'STATUS_CHANGED', 'Revisando el equipo en sitio', te.user_id,
       tk.first_response_at
FROM tickets tk
JOIN technicians te ON te.id = tk.technician_id
WHERE tk.status = 'EN_PROGRESO';
