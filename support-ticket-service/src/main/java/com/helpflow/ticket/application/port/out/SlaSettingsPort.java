package com.helpflow.ticket.application.port.out;

/** Configuración de SLA leída de application.yml (helpflow.sla.*). */
public interface SlaSettingsPort {

    int escalationThresholdPercent();
}
