package com.helpflow.ticket.application.port.in;

import com.helpflow.ticket.domain.model.AccountStatus;
import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.Technician;

import java.util.List;

public interface ManageTechnicianUseCase {

    /** Crea el usuario con rol TECNICO y su perfil de soporte (código TEC-xxx autogenerado). */
    Technician create(CreateTechnicianCommand command);

    List<Technician> list();

    Technician update(Long technicianId, UpdateTechnicianCommand command);

    record CreateTechnicianCommand(String document, String name, String email, String password, String sede,
                                   Category specialty, int maxActiveTickets) {
    }

    record UpdateTechnicianCommand(Category specialty, String sede, int maxActiveTickets, AccountStatus status) {
    }
}
