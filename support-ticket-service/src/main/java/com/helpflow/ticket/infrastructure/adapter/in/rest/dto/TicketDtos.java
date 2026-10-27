package com.helpflow.ticket.infrastructure.adapter.in.rest.dto;

import com.helpflow.ticket.domain.model.Category;
import com.helpflow.ticket.domain.model.HistoryAction;
import com.helpflow.ticket.domain.model.Priority;
import com.helpflow.ticket.domain.model.TicketStatus;
import com.helpflow.ticket.infrastructure.adapter.in.rest.validation.ValidStatusChange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class TicketDtos {

    private TicketDtos() {
    }

    public record CreateTicketRequest(
            @NotBlank @Size(min = 5, max = 150) String title,
            @NotBlank @Size(min = 10, max = 2000) String description,
            @NotNull Category category,
            @NotNull Priority priority,
            @NotBlank @Size(max = 60) String sede,
            @NotBlank @Size(max = 120) String affectedAsset
    ) {
    }

    /** Validación cruzada a nivel de clase: ver {@link ValidStatusChange}. */
    @ValidStatusChange
    public record ChangeStatusRequest(
            @NotNull TicketStatus status,
            @NotBlank @Size(min = 5, max = 500) String observation
    ) {
    }

    public record ReassignRequest(
            @NotNull @Positive Long technicianId,
            @NotBlank @Size(min = 5, max = 500) String observation
    ) {
    }

    public record TicketResponse(Long id, Long requesterId, String title, String description, Category category,
                                 Priority priority, String sede, String affectedAsset, TicketStatus status,
                                 Long technicianId, boolean escalated, Instant createdAt, Instant slaDeadline,
                                 Instant firstResponseAt, Instant resolvedAt) {
    }

    public record TicketSummaryResponse(Long id, String title, Category category, Priority priority,
                                        TicketStatus status, String sede, boolean escalated, Instant createdAt,
                                        Instant slaDeadline, Long requesterId, String requesterName,
                                        Long technicianId, String technicianCode) {
    }

    public record HistoryResponse(Long id, TicketStatus fromStatus, TicketStatus toStatus, HistoryAction action,
                                  String observation, Long performedBy, Instant createdAt) {
    }

    public record EscalationResponse(int escalated) {
    }
}
