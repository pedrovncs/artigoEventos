package com.infnet.artigoEventos.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser
    @DisplayName("GET /api/teste - deve retornar 200 quando o banco responde corretamente")
    void testHealthCheckSuccess() throws Exception {

        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        mockMvc.perform(get("/api/teste/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK rodando... :D"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/teste - deve retornar 503 quando ocorre falha no banco")
    void testHealthCheckDatabaseError() throws Exception {

        doThrow(new RuntimeException("Erro DB"))
                .when(jdbcTemplate).queryForObject("SELECT 1", Integer.class);

        mockMvc.perform(get("/api/teste/healthcheck"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("api indisponível... falha no banco de dados xP"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/teste/deploy retorna 200 quando deploy.html existe")
    void testGetDeployStatusFileExists() throws Exception {
        mockMvc.perform(get("/api/teste/deploy"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/teste/deploy retorna 404 quando deploy.html não existe")
    void testGetDeployStatusFileNotFound() throws Exception {
        mockMvc.perform(get("/api/teste/deployFail"))
                .andExpect(status().isNotFound());
    }
}
import static org.junit.jupiter.api.Assertions.*;

class ApiControllerTest {

}
