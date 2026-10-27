package com.helpflow.ticket.infrastructure.adapter.in.rest.mapper;

import com.helpflow.ticket.application.port.in.CreateTicketUseCase.CreateTicketCommand;
import com.helpflow.ticket.application.port.in.LoginUseCase.LoginCommand;
import com.helpflow.ticket.application.port.in.ManageTechnicianUseCase.CreateTechnicianCommand;
import com.helpflow.ticket.application.port.in.ManageTechnicianUseCase.UpdateTechnicianCommand;
import com.helpflow.ticket.application.port.in.ManageUserUseCase.UpdateProfileCommand;
import com.helpflow.ticket.application.port.in.RegisterUserUseCase.RegisterCommand;
import com.helpflow.ticket.domain.model.SlaPolicy;
import com.helpflow.ticket.domain.model.Technician;
import com.helpflow.ticket.domain.model.Ticket;
import com.helpflow.ticket.domain.model.TicketHistory;
import com.helpflow.ticket.domain.model.TicketSummary;
import com.helpflow.ticket.domain.model.User;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.AuthDtos.LoginRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.AuthDtos.RegisterRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.SlaPolicyDtos.SlaPolicyResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.CreateTechnicianRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.TechnicianResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TechnicianDtos.UpdateTechnicianRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.CreateTicketRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.HistoryResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.TicketResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.TicketDtos.TicketSummaryResponse;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.UpdateProfileRequest;
import com.helpflow.ticket.infrastructure.adapter.in.rest.dto.UserDtos.UserResponse;
import org.mapstruct.Mapper;

/** DTO <-> comandos y dominio -> DTO de respuesta. Sin lógica de negocio. */
@Mapper
public interface RestMapper {

    RegisterCommand toCommand(RegisterRequest request);

    LoginCommand toCommand(LoginRequest request);

    UpdateProfileCommand toCommand(UpdateProfileRequest request);

    CreateTechnicianCommand toCommand(CreateTechnicianRequest request);

    UpdateTechnicianCommand toCommand(UpdateTechnicianRequest request);

    CreateTicketCommand toCommand(CreateTicketRequest request);

    UserResponse toResponse(User user);

    TechnicianResponse toResponse(Technician technician);

    TicketResponse toResponse(Ticket ticket);

    TicketSummaryResponse toResponse(TicketSummary summary);

    HistoryResponse toResponse(TicketHistory history);

    SlaPolicyResponse toResponse(SlaPolicy policy);
}
