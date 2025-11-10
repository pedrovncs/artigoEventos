package com.infnet.artigoEventos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import com.infnet.artigoEventos.service.EventoService;
import jakarta.persistence.EntityNotFoundException;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.hibernate.annotations.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    Teste de Integração para o EventoController.
    @WebMvcTest APENAS para a camada web (o controller).
    O EventoService mockado para isolar os testes do controller.
*/

/*  
    Informa ao Spring para testar APENAS este controller.
    Simula um usuário logado para todos os testes.
    Ferramenta para converter objetos Java <-> JSON
*/
@WebMvcTest(EventoController.class)
@WithMockUser(roles = "ADMIN")
public class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // 

    @MockBean
    private EventoService eventoService;

    /*  
    *   Teste para o endpoint GET /api/eventos
    *  Verifica se o status é 200 OK e se o tamanho da lista retornada é 2.
    */
    @Test
    @Comment("Teste para o endpoint GET /api/eventos para listar todos os eventos")  
    public void testGetAllEventos() throws Exception {
        when(eventoService.getAllEventos()).thenReturn(List.of(new Evento(), new Evento()));

        mockMvc.perform(get("/api/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    /*  
      *   Teste para o endpoint GET /api/eventos/{id}
      *  Verifica se o status é 200 OK e se o evento retornado tem os valores esperados.
    */
    @Test
    @Comment("Teste para o endpoint GET /api/eventos/{id} para obter um evento por ID")
    public void testGetEventoById_Success() throws Exception {
        Evento mockEvento = new Evento();
        mockEvento.setId(1);
        mockEvento.setNome("Evento de Teste");
        when(eventoService.getEventoById(1)).thenReturn(mockEvento);

        mockMvc.perform(get("/api/eventos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Evento de Teste"));
    }

    /*  
      *   Teste para o endpoint GET /api/eventos/{id}
      *  Verifica se o status é 404 NOT_FOUND e se a mensagem de erro está correta.
    */
    @Test
    @Comment("Teste para o endpoint GET /api/eventos/{id} quando o evento não é encontrado")
    public void testGetEventoById_NotFound() throws Exception {

        when(eventoService.getEventoById(99)).thenThrow(new EntityNotFoundException("Evento 99 não encontrado"));

        mockMvc.perform(get("/api/eventos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Evento 99 não encontrado"));
    }

    /*  
      *   Teste para o endpoint POST /api/eventos
      *  Verifica se o status é 201 CREATED e se o evento retornado tem os valores esperados.
    */
    @Test
    @Comment("Teste para o endpoint POST /api/eventos para criar um novo evento")
    @WithMockUser(roles = "ADMIN") // Garante a permissão
    public void testCreateEvento() throws Exception {
        // 1. O DTO (JSON) que será enviado
        EventoCreateDto dto = new EventoCreateDto();
        dto.setNome("Novo Evento");
        
        dto.setDataEvento(LocalDateTime.now().plusDays(1)); // @FutureOrPresent
        dto.setLocalEvento("Local de Teste");               // @NotBlank
        dto.setOrganizadorId(1);                            // @NotNull

        String dtoAsJson = objectMapper.writeValueAsString(dto);

        // 2. O arquivo de imagem (MultipartFile)
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem", 
                "imagem-teste.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo-da-imagem".getBytes()
        );

        // 3. O JSON (como MultipartFile)
        MockMultipartFile eventoJsonPart = new MockMultipartFile(
                "evento",
                "", 
                MediaType.APPLICATION_JSON_VALUE,
                dtoAsJson.getBytes(StandardCharsets.UTF_8)
        );

        // 4. O objeto que o service "mockado" deve retornar
        Evento eventoSalvo = new Evento();
        eventoSalvo.setId(1);
        eventoSalvo.setNome("Novo Evento");
        when(eventoService.createEvento(any(EventoCreateDto.class), any(MultipartFile.class)))
                .thenReturn(eventoSalvo);

        // 5. Executa o teste
        mockMvc.perform(multipart("/api/eventos") 
                        .file(imagem)
                        .file(eventoJsonPart) 
                        .with(csrf())) // CSRF
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Novo Evento"));
    }

    /* 
     *  Teste para o endpoint PUT /api/eventos/{id}
     *  Verifica se o status é 200 OK e se o evento retornado tem os valores esperados.
    */
    @Test
    @Comment("Teste para o endpoint PUT /api/eventos/{id} para atualizar um evento existente")
    public void testUpdateEvento() throws Exception {
        EventoUpdateDto dto = new EventoUpdateDto();
        dto.setNome("Nome Atualizado");
        String dtoAsJson = objectMapper.writeValueAsString(dto);

        Evento eventoAtualizado = new Evento();
        eventoAtualizado.setId(1);
        eventoAtualizado.setNome("Nome Atualizado");

        when(eventoService.updateEvento(eq(1), any(EventoUpdateDto.class))).thenReturn(eventoAtualizado);

        mockMvc.perform(put("/api/eventos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(dtoAsJson))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    /* 
     *  Teste para o endpoint DELETE /api/eventos/{id}
     * Verifica se o status é 204 NO_CONTENT.
     */
    @Test
    @Comment("Teste para o endpoint DELETE /api/eventos/{id} para deletar um evento")
    public void testDeleteEvento() throws Exception {
        doNothing().when(eventoService).deleteEvento(1);

        mockMvc.perform(delete("/api/eventos/1").with(csrf()))
                .andExpect(status().isNoContent()); 
    }

    /* 
     *  Teste para o endpoint POST /api/eventos/{id}/participantes
     *  Verifica se o status é 201 CREATED e se o participante retornado tem os valores esperados.
    */
    @Test
    @Comment("Teste para o endpoint POST /api/eventos/{id}/participantes para adicionar um participante")
    @WithMockUser(roles = "ADMIN")
    public void testAddParticipante() throws Exception {
        ParticipanteDto dto = new ParticipanteDto();
        dto.setEmail("teste@email.com");
        dto.setNome("Nome do Participante"); // @NotBlank

        String dtoAsJson = objectMapper.writeValueAsString(dto);

        Participante participanteSalvo = new Participante();
        participanteSalvo.setId(1);
        participanteSalvo.setEmail("teste@email.com");

        when(eventoService.addParticipante(eq(1), any(ParticipanteDto.class))).thenReturn(participanteSalvo);

        mockMvc.perform(post("/api/eventos/1/participantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()) 
                        .content(dtoAsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }
}