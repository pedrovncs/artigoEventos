package com.infnet.artigoEventos.controller.evento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.artigoEventos.controller.EventoController;
import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import com.infnet.artigoEventos.model.StatusEvento;
import com.infnet.artigoEventos.service.EventoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static com.infnet.artigoEventos.controller.evento.EventoControllerTestConstants.*;
import static com.infnet.artigoEventos.controller.evento.EventoTestDataBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para o EventoController.
 * Organizado em classes @Nested para melhor estruturação e legibilidade.
 * Utiliza constantes e builders de classes auxiliares para reduzir duplicação.
 */
@WebMvcTest(EventoController.class)
class EventoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private EventoService eventoService;

        @Autowired
        private ObjectMapper objectMapper;

        // ==================================================================================
        // TESTES DE LEITURA (GET)
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Leitura (GET)")
        class GetTests {

                @Test
                @WithMockUser
                @DisplayName("GET /api/eventos - deve retornar lista de eventos")
                void testGetAllEventos() throws Exception {
                        Evento evento = createMockEvento(EVENTO_ID_EXISTENTE, EVENTO_NOME_TESTE);

                        Mockito.when(eventoService.getAllEventos()).thenReturn(List.of(evento));

                        mockMvc.perform(get("/api/eventos"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0].nome").value(EVENTO_NOME_TESTE));

                        verify(eventoService).getAllEventos();
                }

                @Test
                @WithMockUser
                @DisplayName("GET /api/eventos/{id} - sucesso")
                void testGetEventoById_Success() throws Exception {
                        Evento evento = createMockEvento(EVENTO_ID_EXISTENTE, EVENTO_NOME_TESTE);

                        Mockito.when(eventoService.getEventoById(EVENTO_ID_EXISTENTE)).thenReturn(evento);

                        mockMvc.perform(get("/api/eventos/" + EVENTO_ID_EXISTENTE))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.nome").value(EVENTO_NOME_TESTE));

                        verify(eventoService).getEventoById(EVENTO_ID_EXISTENTE);
                }

                @Test
                @WithMockUser
                @DisplayName("GET /api/eventos/{id} - evento não encontrado")
                void testGetEventoById_NotFound() throws Exception {
                        Mockito.when(eventoService.getEventoById(EVENTO_ID_INEXISTENTE))
                                        .thenThrow(new EntityNotFoundException(MSG_EVENTO_NAO_ENCONTRADO));

                        mockMvc.perform(get("/api/eventos/" + EVENTO_ID_INEXISTENTE))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().string(MSG_EVENTO_NAO_ENCONTRADO));
                }
        }

        // ==================================================================================
        // TESTES DE CRIAÇÃO (POST)
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Criação (POST)")
        class PostTests {

                @Test
                @WithMockUser(username = EMAIL_USUARIO_TESTE, roles = { "USER" })
                @DisplayName("POST /api/eventos - criar evento com sucesso")
                void testCreateEvento_Success() throws Exception {
                        EventoCreateDto dto = createEventoCreateDto(
                                        EVENTO_NOME_TESTE,
                                        EVENTO_LOCAL_SP,
                                        LocalDateTime.now().plusDays(1));

                        MockMultipartFile dtoPart = createJsonMultipartFile("evento", dto, objectMapper);
                        MockMultipartFile imagem = createImageMultipartFile();

                        Evento salvo = createMockEvento(EVENTO_ID_NOVO, EVENTO_NOME_TESTE);

                        Mockito.when(eventoService.createEvento(any(), any(), eq(EMAIL_USUARIO_TESTE)))
                                        .thenReturn(salvo);

                        mockMvc.perform(multipart("/api/eventos")
                                        .file(dtoPart)
                                        .file(imagem)
                                        .with(csrf()))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(EVENTO_ID_NOVO))
                                        .andExpect(jsonPath("$.nome").value(EVENTO_NOME_TESTE));

                        verify(eventoService).createEvento(any(), any(), eq(EMAIL_USUARIO_TESTE));
                }

                @Test
                @WithMockUser(username = EMAIL_INEXISTENTE, roles = { "USER" })
                @DisplayName("POST /api/eventos - organizador não encontrado")
                void testCreateEvento_OrganizadorNotFound() throws Exception {
                        EventoCreateDto dto = createEventoCreateDto(
                                        EVENTO_NOME_TESTE,
                                        EVENTO_LOCAL_RJ,
                                        LocalDateTime.now().plusDays(1));

                        MockMultipartFile dtoPart = createJsonMultipartFile("evento", dto, objectMapper);

                        Mockito.when(eventoService.createEvento(any(), any(), eq(EMAIL_INEXISTENTE)))
                                        .thenThrow(new EntityNotFoundException(MSG_USUARIO_ORGANIZADOR_NAO_ENCONTRADO));

                        mockMvc.perform(multipart("/api/eventos")
                                        .file(dtoPart)
                                        .with(csrf()))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().string(MSG_USUARIO_ORGANIZADOR_NAO_ENCONTRADO));
                }

                @Test
                @WithMockUser(username = EMAIL_USUARIO_TESTE, roles = { "USER" })
                @DisplayName("POST /api/eventos - erro de validação")
                void testCreateEvento_InvalidData() throws Exception {
                        EventoCreateDto dto = new EventoCreateDto();
                        dto.setNome(""); // Invalid: Blank
                        dto.setLocalEvento(EVENTO_LOCAL_SP);
                        dto.setDescricao(EVENTO_DESCRICAO);
                        dto.setDataEvento(LocalDateTime.now().minusDays(1)); // Invalid: Past

                        MockMultipartFile dtoPart = createJsonMultipartFile("evento", dto, objectMapper);

                        mockMvc.perform(multipart("/api/eventos")
                                        .file(dtoPart)
                                        .with(csrf()))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ==================================================================================
        // TESTES DE ATUALIZAÇÃO (PUT)
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Atualização (PUT)")
        class PutTests {

                @Nested
                @DisplayName("Atualização de Dados")
                class UpdateEventoTests {

                        @Test
                        @WithMockUser
                        @DisplayName("PUT /api/eventos/{id} - atualizar evento com sucesso")
                        void testUpdateEvento_Success() throws Exception {
                                EventoUpdateDto dto = createEventoUpdateDto(EVENTO_NOME_TESTE, StatusEvento.ATIVO);

                                Evento atualizado = createMockEvento(EVENTO_ID_EXISTENTE, EVENTO_NOME_TESTE);

                                Mockito.when(eventoService.updateEvento(eq(EVENTO_ID_EXISTENTE), any()))
                                                .thenReturn(atualizado);

                                mockMvc.perform(put("/api/eventos/" + EVENTO_ID_EXISTENTE)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(dto)))
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.nome").value(EVENTO_NOME_TESTE));

                                verify(eventoService).updateEvento(eq(EVENTO_ID_EXISTENTE), any());
                        }

                        @Test
                        @WithMockUser
                        @DisplayName("PUT /api/eventos/{id} - evento não encontrado")
                        void testUpdateEvento_NotFound() throws Exception {
                                Mockito.when(eventoService.updateEvento(eq(EVENTO_ID_INEXISTENTE), any()))
                                                .thenThrow(new EntityNotFoundException(MSG_EVENTO_NAO_ENCONTRADO));

                                mockMvc.perform(put("/api/eventos/" + EVENTO_ID_INEXISTENTE)
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{\"nome\":\"x\"}"))
                                                .andExpect(status().isNotFound())
                                                .andExpect(content().string(MSG_EVENTO_NAO_ENCONTRADO));
                        }
                }

                @Nested
                @DisplayName("Atualização de Imagem")
                class UpdateImagemTests {

                        @Test
                        @WithMockUser
                        @DisplayName("PUT /api/eventos/{id}/imagem - atualizar imagem do evento")
                        void testUpdateEventoImagem_Success() throws Exception {
                                MockMultipartFile file = createImageMultipartFile();

                                Evento evento = createMockEvento(EVENTO_ID_EXISTENTE, EVENTO_NOME_TESTE);

                                Mockito.when(eventoService.updateEventoImagem(eq(EVENTO_ID_EXISTENTE), any()))
                                                .thenReturn(evento);

                                mockMvc.perform(multipart("/api/eventos/" + EVENTO_ID_EXISTENTE + "/imagem")
                                                .file(file)
                                                .with(request -> {
                                                        request.setMethod("PUT");
                                                        return request;
                                                })
                                                .with(csrf()))
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.id").value(EVENTO_ID_EXISTENTE));

                                verify(eventoService).updateEventoImagem(eq(EVENTO_ID_EXISTENTE), any());
                        }

                        @Test
                        @WithMockUser
                        @DisplayName("PUT /api/eventos/{id}/imagem - evento não encontrado")
                        void testUpdateEventoImagem_NotFound() throws Exception {
                                MockMultipartFile file = createImageMultipartFile();

                                Mockito.when(eventoService.updateEventoImagem(eq(EVENTO_ID_INEXISTENTE), any()))
                                                .thenThrow(new EntityNotFoundException(MSG_EVENTO_NAO_ENCONTRADO));

                                mockMvc.perform(multipart("/api/eventos/" + EVENTO_ID_INEXISTENTE + "/imagem")
                                                .file(file)
                                                .with(request -> {
                                                        request.setMethod("PUT");
                                                        return request;
                                                })
                                                .with(csrf()))
                                                .andExpect(status().isNotFound())
                                                .andExpect(content().string(MSG_EVENTO_NAO_ENCONTRADO));
                        }

                        @Test
                        @WithMockUser
                        @DisplayName("PUT /api/eventos/{id}/imagem - erro ao processar imagem")
                        void testUpdateEventoImagem_IOException() throws Exception {
                                MockMultipartFile file = createImageMultipartFile();

                                Mockito.when(eventoService.updateEventoImagem(eq(EVENTO_ID_EXISTENTE), any()))
                                                .thenThrow(new IOException("Erro ao salvar imagem"));

                                mockMvc.perform(multipart("/api/eventos/" + EVENTO_ID_EXISTENTE + "/imagem")
                                                .file(file)
                                                .with(request -> {
                                                        request.setMethod("PUT");
                                                        return request;
                                                })
                                                .with(csrf()))
                                                .andExpect(status().isInternalServerError())
                                                .andExpect(content().string(MSG_ERRO_PROCESSANDO_IMAGEM));
                        }
                }
        }

        // ==================================================================================
        // TESTES DE EXCLUSÃO (DELETE)
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Exclusão (DELETE)")
        class DeleteTests {

                @Test
                @WithMockUser
                @DisplayName("DELETE /api/eventos/{id} - deletar evento com sucesso")
                void testDeleteEvento_Success() throws Exception {
                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_EXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isNoContent());

                        verify(eventoService).deleteEvento(EVENTO_ID_EXISTENTE);
                }

                @Test
                @WithMockUser
                @DisplayName("DELETE /api/eventos/{id} - evento não encontrado")
                void testDeleteEvento_NotFound() throws Exception {
                        Mockito.doThrow(new EntityNotFoundException(MSG_EVENTO_NAO_ENCONTRADO))
                                        .when(eventoService).deleteEvento(EVENTO_ID_INEXISTENTE);

                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_INEXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().string(MSG_EVENTO_NAO_ENCONTRADO));
                }
        }

        // ==================================================================================
        // TESTES DE PARTICIPANTES
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Participantes")
        class ParticipantesTests {

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("POST /api/eventos/{id}/participantes - adicionar participante")
                void testAddParticipante_Success() throws Exception {
                        ParticipanteDto dto = createParticipanteDto(PARTICIPANTE_NOME, PARTICIPANTE_EMAIL);

                        Participante participante = createMockParticipante(PARTICIPANTE_ID_NOVO);

                        Mockito.when(eventoService.addParticipante(eq(EVENTO_ID_EXISTENTE), any(),
                                        eq(EMAIL_ORGANIZADOR)))
                                        .thenReturn(participante);

                        mockMvc.perform(post("/api/eventos/" + EVENTO_ID_EXISTENTE + "/participantes")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(PARTICIPANTE_ID_NOVO));

                        verify(eventoService).addParticipante(eq(EVENTO_ID_EXISTENTE), any(), eq(EMAIL_ORGANIZADOR));
                }

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("POST /api/eventos/{id}/participantes - evento não encontrado")
                void testAddParticipante_EventoNotFound() throws Exception {
                        ParticipanteDto dto = createParticipanteDto(PARTICIPANTE_NOME, PARTICIPANTE_EMAIL);

                        Mockito.when(eventoService.addParticipante(eq(EVENTO_ID_INEXISTENTE), any(),
                                        eq(EMAIL_ORGANIZADOR)))
                                        .thenThrow(new EntityNotFoundException(MSG_EVENTO_NAO_ENCONTRADO));

                        mockMvc.perform(post("/api/eventos/" + EVENTO_ID_INEXISTENTE + "/participantes")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().string(MSG_EVENTO_NAO_ENCONTRADO));
                }

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("POST /api/eventos/{id}/participantes - erro de regra de negócio")
                void testAddParticipante_BadRequest() throws Exception {
                        ParticipanteDto dto = createParticipanteDto(PARTICIPANTE_NOME, PARTICIPANTE_EMAIL);

                        Mockito.when(eventoService.addParticipante(eq(EVENTO_ID_EXISTENTE), any(),
                                        eq(EMAIL_ORGANIZADOR)))
                                        .thenThrow(new IllegalArgumentException(MSG_PARTICIPANTE_JA_INSCRITO));

                        mockMvc.perform(post("/api/eventos/" + EVENTO_ID_EXISTENTE + "/participantes")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(content().string(MSG_PARTICIPANTE_JA_INSCRITO));
                }

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - remover participante")
                void testRemoveParticipante_Success() throws Exception {
                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_EXISTENTE + "/participantes/"
                                        + PARTICIPANTE_ID_EXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isNoContent());

                        verify(eventoService).removeParticipante(EVENTO_ID_EXISTENTE, PARTICIPANTE_ID_EXISTENTE,
                                        EMAIL_ORGANIZADOR);
                }

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - participante não encontrado")
                void testRemoveParticipante_NotFound() throws Exception {
                        Mockito.doThrow(new EntityNotFoundException(MSG_PARTICIPANTE_NAO_ENCONTRADO))
                                        .when(eventoService).removeParticipante(EVENTO_ID_EXISTENTE,
                                                        PARTICIPANTE_ID_INEXISTENTE, EMAIL_ORGANIZADOR);

                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_EXISTENTE + "/participantes/"
                                        + PARTICIPANTE_ID_INEXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isNotFound())
                                        .andExpect(content().string(MSG_PARTICIPANTE_NAO_ENCONTRADO));
                }

                @Test
                @WithMockUser(username = EMAIL_ORGANIZADOR, roles = { "USER" })
                @DisplayName("DELETE /api/eventos/{id}/participantes/{participanteId} - erro de regra de negócio")
                void testRemoveParticipante_BadRequest() throws Exception {
                        Mockito.doThrow(new IllegalArgumentException(MSG_PARTICIPANTE_NAO_PERTENCE_EVENTO))
                                        .when(eventoService).removeParticipante(EVENTO_ID_EXISTENTE,
                                                        PARTICIPANTE_ID_INEXISTENTE, EMAIL_ORGANIZADOR);

                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_EXISTENTE + "/participantes/"
                                        + PARTICIPANTE_ID_INEXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(content().string(MSG_PARTICIPANTE_NAO_PERTENCE_EVENTO));
                }
        }

        // ==================================================================================
        // TESTES DE SEGURANÇA
        // ==================================================================================

        @Nested
        @DisplayName("Testes de Segurança")
        class SecurityTests {

                @Test
                @DisplayName("POST /api/eventos - não autorizado (sem usuário)")
                void testCreateEvento_Unauthorized() throws Exception {
                        EventoCreateDto dto = createEventoCreateDto(
                                        EVENTO_NOME_TESTE,
                                        EVENTO_LOCAL_SP,
                                        LocalDateTime.now().plusDays(1));

                        MockMultipartFile dtoPart = createJsonMultipartFile("evento", dto, objectMapper);

                        mockMvc.perform(multipart("/api/eventos")
                                        .file(dtoPart)
                                        .with(csrf()))
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("DELETE /api/eventos/{id} - não autorizado (sem usuário)")
                void testDeleteEvento_Unauthorized() throws Exception {
                        mockMvc.perform(delete("/api/eventos/" + EVENTO_ID_EXISTENTE)
                                        .with(csrf()))
                                        .andExpect(status().isUnauthorized());
                }
        }
}
