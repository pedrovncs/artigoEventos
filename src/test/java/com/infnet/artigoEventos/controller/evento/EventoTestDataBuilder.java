package com.infnet.artigoEventos.controller.evento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import com.infnet.artigoEventos.model.StatusEvento;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static com.infnet.artigoEventos.controller.evento.EventoControllerTestConstants.*;

import java.time.LocalDateTime;

/**
 * Classe utilitária para construção de objetos de teste.
 * Fornece métodos builders e factories para criar DTOs, entidades e arquivos
 * mock.
 */
public final class EventoTestDataBuilder {

  /**
   * Construtor privado para prevenir instanciação.
   */
  private EventoTestDataBuilder() {
    throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
  }

  // ==================================================================================
  // BUILDERS PARA DTOs
  // ==================================================================================

  /**
   * Cria um EventoCreateDto com valores padrão para testes.
   *
   * @param nome  Nome do evento
   * @param local Local do evento
   * @param data  Data do evento
   * @return EventoCreateDto configurado
   */
  public static EventoCreateDto createEventoCreateDto(String nome, String local, LocalDateTime data) {
    EventoCreateDto dto = new EventoCreateDto();
    dto.setNome(nome);
    dto.setLocalEvento(local);
    dto.setDescricao(EVENTO_DESCRICAO);
    dto.setDataEvento(data);
    return dto;
  }

  /**
   * Cria um EventoUpdateDto para testes.
   *
   * @param nome   Nome atualizado do evento
   * @param status Status do evento
   * @return EventoUpdateDto configurado
   */
  public static EventoUpdateDto createEventoUpdateDto(String nome, StatusEvento status) {
    EventoUpdateDto dto = new EventoUpdateDto();
    dto.setNome(nome);
    dto.setStatus(status);
    return dto;
  }

  /**
   * Cria um ParticipanteDto para testes.
   *
   * @param nome  Nome do participante
   * @param email Email do participante
   * @return ParticipanteDto configurado
   */
  public static ParticipanteDto createParticipanteDto(String nome, String email) {
    ParticipanteDto dto = new ParticipanteDto();
    dto.setNome(nome);
    dto.setEmail(email);
    return dto;
  }

  // ==================================================================================
  // FACTORIES PARA ENTIDADES MOCK
  // ==================================================================================

  /**
   * Cria um Evento mock para testes.
   *
   * @param id   ID do evento
   * @param nome Nome do evento
   * @return Evento mock configurado
   */
  public static Evento createMockEvento(Integer id, String nome) {
    Evento evento = new Evento();
    evento.setId(id);
    evento.setNome(nome);
    return evento;
  }

  /**
   * Cria um Participante mock para testes.
   *
   * @param id ID do participante
   * @return Participante mock configurado
   */
  public static Participante createMockParticipante(Integer id) {
    Participante participante = new Participante();
    participante.setId(id);
    return participante;
  }

  // ==================================================================================
  // FACTORIES PARA ARQUIVOS MOCK
  // ==================================================================================

  /**
   * Cria um MockMultipartFile em formato JSON.
   *
   * @param name         Nome do parâmetro multipart
   * @param content      Objeto a ser serializado como JSON
   * @param objectMapper ObjectMapper para serialização
   * @return MockMultipartFile configurado
   * @throws Exception Se houver erro na serialização
   */
  public static MockMultipartFile createJsonMultipartFile(String name, Object content, ObjectMapper objectMapper)
      throws Exception {
    return new MockMultipartFile(
        name, "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(content));
  }

  /**
   * Cria um MockMultipartFile de imagem para testes.
   *
   * @return MockMultipartFile de imagem configurado
   */
  public static MockMultipartFile createImageMultipartFile() {
    return new MockMultipartFile(
        "imagem", IMAGEM_FILENAME, IMAGEM_CONTENT_TYPE,
        IMAGEM_FAKE_CONTENT.getBytes());
  }
}
