package com.helpflow.ticket.domain.model;

import com.helpflow.ticket.domain.exception.BusinessRuleException;
import com.helpflow.ticket.domain.exception.InactiveUserException;
import com.helpflow.ticket.domain.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de dominio puras: sin mocks ni Spring. */
class UserTest {

    @Test
    void creaUsuarioActivoConCorreoNormalizado() {
        User user = User.create("123456", "Ana", "  Ana@HelpFlow.com ", "medellin", "TI", "hash", Role.USUARIO,
                TestData.NOW);

        assertThat(user.getEmail()).isEqualTo("ana@helpflow.com");
        assertThat(user.getSede()).isEqualTo("MEDELLIN");
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void rechazaCorreoNoCorporativo() {
        assertThatThrownBy(() -> User.create("123456", "Ana", "ana@gmail.com", "MEDELLIN", "TI", "hash",
                Role.USUARIO, TestData.NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("corporativo");
    }

    @Test
    void usuarioInactivoNoPuedeOperar() {
        assertThatThrownBy(() -> TestData.inactiveUser(1L).ensureActive())
                .isInstanceOf(InactiveUserException.class);
    }
}
