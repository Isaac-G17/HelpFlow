package com.helpflow.ticket.infrastructure.adapter.in.scheduler;

import com.helpflow.ticket.application.port.in.EscalateTicketsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Adapter de ENTRADA (como un controller, pero disparado por tiempo): invoca el caso de uso de escalamiento. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "helpflow.sla", name = "scheduler-enabled", havingValue = "true")
public class SlaEscalationScheduler {

    private final EscalateTicketsUseCase escalateTicketsUseCase;

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "${helpflow.sla.escalation-interval}")
    public void escalate() {
        try {
            int escalated = escalateTicketsUseCase.escalateNearBreach();
            log.info("Scheduler SLA: {} tickets escalados", escalated);
        } catch (UnsupportedOperationException e) {
            log.warn("Scheduler SLA omitido: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Scheduler SLA falló", e);
        }
    }
}
