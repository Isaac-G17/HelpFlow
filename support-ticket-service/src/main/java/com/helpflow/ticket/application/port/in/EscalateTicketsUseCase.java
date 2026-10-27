package com.helpflow.ticket.application.port.in;

public interface EscalateTicketsUseCase {

    /** Escala los tickets activos próximos a incumplir el SLA. Devuelve cuántos escaló. */
    int escalateNearBreach();
}
