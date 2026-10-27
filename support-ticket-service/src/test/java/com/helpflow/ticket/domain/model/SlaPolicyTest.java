package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlaPolicyTest {

    @Test
    void calculaLaFechaLimiteDesdeLaCreacion() {
        SlaPolicy policy = new SlaPolicy(Priority.ALTA, 2, 8);

        assertThat(policy.resolutionDeadlineFrom(TestData.NOW)).isEqualTo(TestData.NOW.plusSeconds(8 * 3600));
    }

    @Test
    void laResolucionDebeSerMayorQueLaRespuesta() {
        assertThatThrownBy(() -> new SlaPolicy(Priority.ALTA, 8, 8)).isInstanceOf(BusinessRuleException.class);
    }
}
