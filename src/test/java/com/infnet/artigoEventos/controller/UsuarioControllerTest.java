package com.infnet.artigoEventos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.artigoEventos.dto.LoginDto;
import com.infnet.artigoEventos.dto.UsuarioDto;
import com.infnet.artigoEventos.dto.UsuarioUpdateDto;
import com.infnet.artigoEventos.model.Usuario;
import com.infnet.artigoEventos.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("@POST /signup - criar usuário com sucesso")
    @WithMockUser
    void testSignup_Success() throws Exception {
        UsuarioDto dto = new UsuarioDto();
        dto.setNome("Gabriel");
        dto.setEmail("gabriel@test.com");
        dto.setSenha("123456");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Gabriel");
        usuario.setEmail("gabriel@test.com");

        Mockito.when(usuarioService.signup(any())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Gabriel"));
    }

    @Test
    @DisplayName("@POST /signup - email duplicado")
    @WithMockUser
    void testSignup_EmailJaExiste() throws Exception {
        UsuarioDto dto = new UsuarioDto();
        dto.setNome("João");
        dto.setEmail("joao@test.com");
        dto.setSenha("123456");

        Mockito.when(usuarioService.signup(any()))
                .thenThrow(new IllegalArgumentException("Email já existe"));

        mockMvc.perform(post("/api/usuarios/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email já existe"));
    }

    @Test
    @DisplayName("@POST /login - sucesso")
    @WithMockUser
    void testLogin_Success() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setEmail("teste@x.com");
        dto.setSenha("123456");

        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNome("Teste");
        usuario.setEmail("teste@x.com");

        Mockito.when(usuarioService.login(any())).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("teste@x.com"));
    }

    @Test
    @DisplayName("@POST /login - credenciais inválidas")
    @WithMockUser
    void testLogin_InvalidCredentials() throws Exception {
        LoginDto dto = new LoginDto();
        dto.setEmail("errado@test.com");
        dto.setSenha("123456");

        Mockito.when(usuarioService.login(any()))
                .thenThrow(new IllegalArgumentException("Credenciais inválidas"));

        mockMvc.perform(post("/api/usuarios/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas"));
    }

    @Test
    @DisplayName("@PUT /{id} - atualizar com sucesso")
    @WithMockUser
    void testUpdateUsuario_Success() throws Exception {
        UsuarioUpdateDto dto = new UsuarioUpdateDto();
        dto.setNome("Atualizado");
        dto.setSenha("novasenha");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Atualizado");
        usuario.setEmail("teste@x.com");

        Mockito.when(usuarioService.updateUsuario(eq(1), any())).thenReturn(usuario);

        mockMvc.perform(put("/api/usuarios/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Atualizado"));
    }

    @Test
    @DisplayName("@PUT /{id} - usuário não encontrado")
    @WithMockUser
    void testUpdateUsuario_NotFound() throws Exception {
        UsuarioUpdateDto dto = new UsuarioUpdateDto();
        dto.setNome("Desconhecido");

        Mockito.when(usuarioService.updateUsuario(eq(99), any()))
                .thenThrow(new EntityNotFoundException("Usuário não existe"));

        mockMvc.perform(put("/api/usuarios/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não existe"));
    }

    @Test
    @DisplayName("@DELETE /{id} - remover com sucesso")
    @WithMockUser
    void testDeleteUsuario_Success() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("@DELETE /{id} - usuário não encontrado")
    @WithMockUser
    void testDeleteUsuario_NotFound() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Usuário não encontrado"))
                .when(usuarioService).deleteUsuario(99);

        mockMvc.perform(delete("/api/usuarios/99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuário não encontrado"));
    }
}
import static org.junit.jupiter.api.Assertions.*;

class UsuarioControllerTest {

}
