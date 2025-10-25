package com.infnet.artigoEventos.service;

import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.StatusEvento;
import com.infnet.artigoEventos.model.Usuario;
import com.infnet.artigoEventos.repository.EventoRepository;
import com.infnet.artigoEventos.repository.ParticipanteRepository;
import com.infnet.artigoEventos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EventoServiceTest {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ParticipanteRepository participanteRepository;

    private Usuario usuario;
    private EventoCreateDto eventoDTO;
    private Evento evento;

    @BeforeEach
    void setUp() throws IOException {
        participanteRepository.deleteAll();
        eventoRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Organizador Teste");
        usuario.setEmail("organizador@test.com");
        usuario.setSenha("123456");
        usuario = usuarioRepository.save(usuario);

        eventoDTO = new EventoCreateDto();
        eventoDTO.setNome("Evento Teste");
        eventoDTO.setDataEvento(LocalDateTime.now());
        eventoDTO.setLocalEvento("Local Teste");
        eventoDTO.setDescricao("Descricao Teste");
        eventoDTO.setOrganizadorId(usuario.getId());

        evento = eventoService.createEvento(eventoDTO, null);
    }

    @Test
    void testCreateEventoWithImage() throws IOException {
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem", "teste.jpg", "image/jpeg", "conteudo da imagem".getBytes()
        );

        Evento eventoCriado = eventoService.createEvento(eventoDTO, imagem);

        assertNotNull(eventoCriado.getId());
        assertEquals(eventoDTO.getNome(), eventoCriado.getNome());
        assertEquals(StatusEvento.ATIVO, eventoCriado.getStatus());
        assertNotNull(eventoCriado.getImagemPath());
    }

    @Test
    void testAddParticipante() {
        ParticipanteDto participanteDto = new ParticipanteDto();
        participanteDto.setNome("Participante Teste");
        participanteDto.setEmail("participante@test.com");

        var participante = eventoService.addParticipante(evento.getId(), participanteDto);

        assertNotNull(participante.getId());
        assertEquals("Participante Teste", participante.getNome());
    }

    @Test
    void testUpdateEvento() {
        EventoUpdateDto updateDto = new EventoUpdateDto();
        updateDto.setNome("Evento Atualizado");
        updateDto.setStatus(StatusEvento.CANCELADO);

        Evento atualizado = eventoService.updateEvento(evento.getId(), updateDto);

        assertEquals("Evento Atualizado", atualizado.getNome());
        assertEquals(StatusEvento.CANCELADO, atualizado.getStatus());
    }

    @Test
    void testDeleteEvento() {
        eventoService.deleteEvento(evento.getId());

        assertFalse(eventoRepository.findById(evento.getId()).isPresent());
    }

    @Test
    void testGetAllEventos() {
        List<Evento> eventos = eventoService.getAllEventos();

        assertFalse(eventos.isEmpty());
        assertEquals(1, eventos.size());
    }

    @Test
    void testRemoveParticipante() {
        ParticipanteDto participanteDto = new ParticipanteDto();
        participanteDto.setNome("Participante Teste");
        participanteDto.setEmail("participante@test.com");

        var participante = eventoService.addParticipante(evento.getId(), participanteDto);

        eventoService.removeParticipante(evento.getId(), participante.getId());

        assertTrue(participanteRepository.findById(participante.getId()).isEmpty());
    }

    @Test
    void testGetEventoImagem() throws IOException {
        MockMultipartFile imagem = new MockMultipartFile(
                "imagem", "teste.jpg", "image/jpeg", "conteudo da imagem".getBytes()
        );

        Evento eventoComImagem = eventoService.createEvento(eventoDTO, imagem);

        byte[] bytes = eventoService.getEventoImagem(eventoComImagem.getId());

        assertArrayEquals("conteudo da imagem".getBytes(), bytes);
    }
}
