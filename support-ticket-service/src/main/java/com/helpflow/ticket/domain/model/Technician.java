package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.exception.TechnicianCapacityExceededException;
import com.helpflow.ticket.domain.model.enums.AccountStatus;
import com.helpflow.ticket.domain.model.enums.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Técnico de soporte: documento, nombre, salario, fecha de afiliación y estado, más su perfil de soporte
 * (especialidad, sede y carga). Documento y nombre pertenecen al usuario TECNICO vinculado (fuente única
 * en la tabla users); aquí se exponen de solo lectura.
 * activeTickets funciona como un "stock": takeTicket() lo sube y releaseTicket() lo baja.
 * version permite el bloqueo optimista (@Version en la entidad JPA): si dos asignaciones
 * concurrentes toman al mismo técnico, la segunda falla con 409.
 */
public class Technician {

    private final Long id;
    private final String code;
    private final Long userId;
    private final String document;
    private final String name;
    private BigDecimal salary;
    private final LocalDate affiliationDate;
    private Category specialty;
    private String sede;
    private AccountStatus status;
    private int maxActiveTickets;
    private int activeTickets;
    private final Long version;

    public Technician(Long id, String code, Long userId, String document, String name, BigDecimal salary,
                      LocalDate affiliationDate, Category specialty, String sede, AccountStatus status,
                      int maxActiveTickets, int activeTickets, Long version) {
        this.id = id;
        this.code = code;
        this.userId = userId;
        this.document = document;
        this.name = name;
        this.salary = salary;
        this.affiliationDate = affiliationDate;
        this.specialty = specialty;
        this.sede = sede;
        this.status = status;
        this.maxActiveTickets = maxActiveTickets;
        this.activeTickets = activeTickets;
        this.version = version;
    }

    /**
     * Crea el técnico a partir de su usuario TECNICO ya registrado.
     *
     * @param today fecha actual (inyectada para poder probar la regla "afiliación no futura")
     */
    public static Technician create(String code, User user, BigDecimal salary, LocalDate affiliationDate,
                                    Category specialty, String sede, int maxActiveTickets, LocalDate today) {
        DomainValidation.requireNonNull(user, "user");
        validateSalary(salary);
        DomainValidation.requireNonNull(affiliationDate, "affiliationDate");
        if (affiliationDate.isAfter(today)) {
            throw new BusinessRuleException("La fecha de afiliación no puede ser futura");
        }
        validateMaxActiveTickets(maxActiveTickets);
        return new Technician(null,
                DomainValidation.requireText(code, "code"),
                DomainValidation.requireNonNull(user.getId(), "userId"),
                user.getDocument(),
                user.getName(),
                salary,
                affiliationDate,
                DomainValidation.requireNonNull(specialty, "specialty"),
                DomainValidation.normalizeSede(sede),
                AccountStatus.ACTIVO,
                maxActiveTickets,
                0,
                null);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVO;
    }

    /**
     * Compatibilidad entre la categoría del ticket y la especialidad del técnico.
     *
     * TODO (tú): implementar.
     *  1. Si la categoría del ticket es OTRO, cualquier especialidad la puede atender -> true.
     *  2. En otro caso, la especialidad del técnico debe ser igual a la categoría.
     */
    public boolean canAttend(Category category) {
        return category == Category.OTRO || specialty == category;
    }

    /**
     * TODO (tú): true si todavía puede recibir otro ticket (activeTickets < maxActiveTickets).
     */
    public boolean hasCapacity(){
        return activeTickets < maxActiveTickets;
    }

    /**
     * El técnico toma un ticket (sube su carga). Equivalente a "descontar stock".
     *
     * TODO (tú): implementar.
     *  1. Si no está activo -> InactiveUserException("El técnico ... está INACTIVO").
     *  2. Si no tiene capacidad -> TechnicianCapacityExceededException(code, maxActiveTickets).
     *  3. activeTickets++.
     */
    public void takeTicket() {
        ensureActive();
        if (!hasCapacity()) {
            throw new TechnicianCapacityExceededException(code, maxActiveTickets);
        }
        activeTickets++;
    }

    /**
     * El técnico libera un ticket (RESUELTO, CANCELADO o reasignado a otro).
     *
     * TODO (tú): implementar.
     *  1. Si activeTickets ya es 0 -> BusinessRuleException (nunca debe quedar negativo).
     *  2. activeTickets--.
     */
    public void releaseTicket() {
        if (activeTickets == 0) {
            throw new BusinessRuleException("El técnico " + code + " no tiene tickets activos para liberar");
        }
        activeTickets--;
    }

    /** Edición del perfil de soporte (ADMIN). La fecha de afiliación no se modifica. */
    public void updateProfile(Category specialty, String sede, BigDecimal salary, int maxActiveTickets,
                              AccountStatus status) {
        validateSalary(salary);
        validateMaxActiveTickets(maxActiveTickets);
        if (maxActiveTickets < activeTickets) {
            throw new BusinessRuleException("La carga máxima (" + maxActiveTickets
                    + ") no puede ser menor que los tickets activos actuales (" + activeTickets + ")");
        }
        this.specialty = DomainValidation.requireNonNull(specialty, "specialty");
        this.sede = DomainValidation.normalizeSede(sede);
        this.salary = salary;
        this.maxActiveTickets = maxActiveTickets;
        this.status = DomainValidation.requireNonNull(status, "status");
    }

    private static void validateSalary(BigDecimal salary) {
        if (salary == null || salary.signum() <= 0) {
            throw new BusinessRuleException("El salario debe ser mayor que 0");
        }
    }

    private static void validateMaxActiveTickets(int maxActiveTickets) {
        if (maxActiveTickets < 1) {
            throw new BusinessRuleException("La carga máxima de tickets debe ser mayor que 0");
        }
    }

    /** Lanza si el técnico está inactivo (útil en reasignaciones manuales). */
    public void ensureActive() {
        if (!isActive()) {
            throw new InactiveUserException("El técnico " + code + " está INACTIVO");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDocument() {
        return document;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public LocalDate getAffiliationDate() {
        return affiliationDate;
    }

    public Category getSpecialty() {
        return specialty;
    }

    public String getSede() {
        return sede;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public int getMaxActiveTickets() {
        return maxActiveTickets;
    }

    public int getActiveTickets() {
        return activeTickets;
    }

    public Long getVersion() {
        return version;
    }
}
