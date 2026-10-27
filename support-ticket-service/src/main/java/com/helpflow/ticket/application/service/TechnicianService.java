package com.helpflow.ticket.application.service;

import com.helpflow.ticket.application.port.in.ManageTechnicianUseCase;
import com.helpflow.ticket.application.port.out.PasswordEncoderPort;
import com.helpflow.ticket.application.port.out.TechnicianRepositoryPort;
import com.helpflow.ticket.application.port.out.UserRepositoryPort;
import com.helpflow.ticket.domain.exception.DuplicateResourceException;
import com.helpflow.ticket.domain.exception.ResourceNotFoundException;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.domain.model.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class TechnicianService implements ManageTechnicianUseCase {

    private static final Logger log = LoggerFactory.getLogger(TechnicianService.class);

    private final TechnicianRepositoryPort technicianRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final Clock clock;

    public TechnicianService(TechnicianRepositoryPort technicianRepository, UserRepositoryPort userRepository,
                             PasswordEncoderPort passwordEncoder, Clock clock) {
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Technician create(CreateTechnicianCommand command) {
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo " + email);
        }
        if (userRepository.existsByDocument(command.document().trim())) {
            throw new DuplicateResourceException("Ya existe un usuario con el documento " + command.document());
        }
        User user = userRepository.save(User.create(command.document(), command.name(), email, command.sede(),
                "Soporte", passwordEncoder.encode(command.password()), Role.TECNICO, clock.instant()));

        Technician technician = technicianRepository.save(Technician.create(nextCode(), user, command.salary(),
                command.affiliationDate(), command.specialty(), command.sede(), command.maxActiveTickets(),
                LocalDate.now(clock)));
        log.info("Técnico creado code={} userId={} specialty={}", technician.getCode(), user.getId(),
                technician.getSpecialty());
        return technician;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technician> list() {
        return technicianRepository.findAll();
    }

    @Override
    @Transactional
    public Technician update(Long technicianId, UpdateTechnicianCommand command) {
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico", technicianId));
        technician.updateProfile(command.specialty(), command.sede(), command.salary(), command.maxActiveTickets(),
                command.status());
        log.info("Perfil de soporte actualizado code={}", technician.getCode());
        return technicianRepository.save(technician);
    }

    /** TEC-011, TEC-012... (la restricción UNIQUE de la BD protege ante una carrera: 409). */
    private String nextCode() {
        long next = technicianRepository.count() + 1;
        String code = "TEC-%03d".formatted(next);
        while (technicianRepository.existsByCode(code)) {
            code = "TEC-%03d".formatted(++next);
        }
        return code;
    }
}
