package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.service.TechnicianDispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TechnicianDispatchController.class)
@Import(TechnicianDispatchService.class)
class TechnicianDispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void respondeConElTecnicoSugerido() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":125,"categoria":"RED","prioridad":"ALTA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(125))
                .andExpect(jsonPath("$.tecnicoId").exists())
                .andExpect(jsonPath("$.disponibilidad").exists())
                .andExpect(jsonPath("$.tiempoEstimadoHoras").isNumber());
    }

    @Test
    void rechazaCategoriaInvalidaCon400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":125,"categoria":"IMPRESORAS","prioridad":"ALTA"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
