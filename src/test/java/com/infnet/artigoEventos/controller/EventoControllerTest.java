package com.infnet.artigoEventos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import com.infnet.artigoEventos.model.StatusEvento;
import com.infnet.artigoEventos.service.EventoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventoService eventoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("GET /api/eventos - deve retornar lista de eventos")
    void testGetAllEventos() throws Exception {
        Evento e = new Evento();
        e.setId(1);
        e.setNome("Teste");

        Mockito.when(eventoService.getAllEventos()).thenReturn(List.of(e));

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Teste"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/eventos/{id} - sucesso")
    void testGetEventoById_Success() throws Exception {
        Evento evento = new Evento();
        evento.setId(1);
        evento.setNome("Evento OK");

        Mockito.when(eventoService.getEventoById(1)).thenReturn(evento);

        mockMvc.perform(get("/api/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Evento OK"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/eventos/{id} - evento não encontrado")
    void testGetEventoById_NotFound() throws Exception {
        Mockito.when(eventoService.getEventoById(99))
                .thenThrow(new EntityNotFoundException("Evento nao encontrado"));

        mockMvc.perform(get("/api/eventos/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Evento nao encontrado"));
    }

    @Test
    @WithMockUser(username = "email@teste.com", roles = {"USER"})
    @DisplayName("POST /api/eventos - criar evento com sucesso")
    void testCreateEvento_Success() throws Exception {
        EventoCreateDto dto = new EventoCreateDto();
        dto.setNome("Novo Evento");
        dto.setLocalEvento("SP");
        dto.setDescricao("Teste");
        dto.setDataEvento(LocalDateTime.now().plusDays(1));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "evento", "", "application/json", objectMapper.writeValueAsBytes(dto)
        );

        MockMultipartFile imagem = new MockMultipartFile(
                "imagem", "foto.png", "image/png", "fake-image".getBytes()
        );

        Evento salvo = new Evento();
        salvo.setId(10);
        salvo.setNome("Novo Evento");

        Mockito.when(eventoService.createEvento(any(), any(), eq("email@teste.com")))
                .thenReturn(salvo);

        mockMvc.perform(multipart("/api/eventos")
                        .file(dtoPart)
                        .file(imagem)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Novo Evento"));
    }

    @Test
    @WithMockUser(username = "naoexiste@x.com", roles = {"USER"})
    @DisplayName("POST /api/eventos - organizador não encontrado")
    void testCreateEvento_OrganizadorNotFound() throws Exception {
        EventoCreateDto dto = new EventoCreateDto();
        dto.setNome("Evento X");
        dto.setLocalEvento("RJ");
        dto.setDescricao("Teste");
        dto.setDataEvento(LocalDateTime.now().plusDays(1));

        MockMultipartFile dtoPart = new MockMultipartFile(
                "evento", "", "application/json", objectMapper.writeValueAsBytes(dto)
        );

        Mockito.when(eventoService.createEvento(any(), any(), eq("naoexiste@x.com")))
                .thenThrow(new EntityNotFoundException("Usuario Organizador nao encotrado"));

        mockMvc.perform(multipart("/api/eventos")
                        .file(dtoPart)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario Organizador nao encotrado"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/eventos/{id} - atualizar evento com sucesso")
    void testUpdateEvento_Success() throws Exception {
        EventoUpdateDto dto = new EventoUpdateDto();
        dto.setNome("Editado");
        dto.setStatus(StatusEvento.ATIVO);

        Evento atualizado = new Evento();
        atualizado.setId(1);
        atualizado.setNome("Editado");

        Mockito.when(eventoService.updateEvento(eq(1), any())).thenReturn(atualizado);

        mockMvc.perform(put("/api/eventos/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Editado"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/eventos/{id} - evento não encontrado")
    void testUpdateEvento_NotFound() throws Exception {
        Mockito.when(eventoService.updateEvento(eq(99), any()))
                .thenThrow(new EntityNotFoundException("Evento não existe"));

        mockMvc.perform(put("/api/eventos/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"x\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Evento não existe"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/eventos/{id}/imagem - atualizar imagem do evento")
    void testUpdateEventoImagem_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "foto.png", "image/png", "img".getBytes()
        );

        Evento evento = new Evento();
        evento.setId(1);

        Mockito.when(eventoService.updateEventoImagem(eq(1), any())).thenReturn(evento);

        mockMvc.perform(multipart("/api/eventos/1/imagem")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/eventos/{id} - deletar evento com sucesso")
    void testDeleteEvento_Success() throws Exception {
        mockMvc.perform(delete("/api/eventos/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("POST /api/eventos/{id}/participantes - adicionar participante")
    void testAddParticipante_Success() throws Exception {
        ParticipanteDto dto = new ParticipanteDto();
        dto.setNome("João");
        dto.setEmail("joao@test.com");

        Participante participante = new Participante();
        participante.setId(1);

        Mockito.when(eventoService.addParticipante(eq(5), any(), eq("organizador@test.com")))
                .thenReturn(participante);

        mockMvc.perform(post("/api/eventos/5/participantes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - remover participante")
    void testRemoveParticipante_Success() throws Exception {
        mockMvc.perform(delete("/api/eventos/3/participantes/20")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/eventos/{id}/imagem - evento não encontrado")
    void testUpdateEventoImagem_NotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "foto.png", "image/png", "img".getBytes()
        );

        Mockito.when(eventoService.updateEventoImagem(eq(99), any()))
                .thenThrow(new EntityNotFoundException("Imagem não encontrada"));

        mockMvc.perform(multipart("/api/eventos/99/imagem")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Imagem não encontrada"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/eventos/{id}/imagem - erro ao processar imagem")
    void testUpdateEventoImagem_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imagem", "foto.png", "image/png", "img".getBytes()
        );

        Mockito.when(eventoService.updateEventoImagem(eq(1), any()))
                .thenThrow(new IOException("Erro ao salvar imagem"));

        mockMvc.perform(multipart("/api/eventos/1/imagem")
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Erro processando a imagem"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/eventos/{id} - evento não encontrado")
    void testDeleteEvento_NotFound() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Evento não existe"))
                .when(eventoService).deleteEvento(99);

        mockMvc.perform(delete("/api/eventos/99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Evento não existe"));
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("POST /api/eventos/{id}/participantes - evento não encontrado")
    void testAddParticipante_EventoNotFound() throws Exception {
        ParticipanteDto dto = new ParticipanteDto();
        dto.setNome("João");
        dto.setEmail("joao@test.com");

        Mockito.when(eventoService.addParticipante(eq(20), any(), eq("organizador@test.com")))
                .thenThrow(new EntityNotFoundException("Evento não encontrado"));

        mockMvc.perform(post("/api/eventos/20/participantes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Evento não encontrado"));
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("POST /api/eventos/{id}/participantes - erro de regra de negócio")
    void testAddParticipante_BadRequest() throws Exception {
        ParticipanteDto dto = new ParticipanteDto();
        dto.setNome("João");
        dto.setEmail("joao@test.com");

        Mockito.when(eventoService.addParticipante(eq(10), any(), eq("organizador@test.com")))
                .thenThrow(new IllegalArgumentException("Participante já inscrito"));

        mockMvc.perform(post("/api/eventos/10/participantes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Participante já inscrito"));
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - participante não encontrado")
    void testRemoveParticipante_NotFound() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Participante não encontrado"))
                .when(eventoService).removeParticipante(1, 200, "organizador@test.com");

        mockMvc.perform(delete("/api/eventos/1/participantes/200")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Participante não encontrado"));
    }

    @Test
    @WithMockUser(username = "organizador@test.com", roles = {"USER"})
    @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - erro de regra de negócio")
    void testRemoveParticipante_BadRequest() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Participante não pertence ao evento"))
                .when(eventoService).removeParticipante(3, 100, "organizador@test.com");

        mockMvc.perform(delete("/api/eventos/3/participantes/100")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Participante não pertence ao evento"));
    }
}
