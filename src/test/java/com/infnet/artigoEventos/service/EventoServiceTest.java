package com.infnet.artigoEventos.service;

import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.*;
import com.infnet.artigoEventos.repository.EventoRepository;
import com.infnet.artigoEventos.repository.ParticipanteRepository;
import com.infnet.artigoEventos.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EventoServiceTest {

    @Autowired
    private EventoService eventoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private ParticipanteRepository participanteRepository;

    private Usuario organizador;
    private EventoCreateDto eventoDto;
    private Evento eventoModel;
    private MockMultipartFile sampleImage;

    private MockMultipartFile loadSampleImage(int imageNumber) throws IOException {
        Path imagePath = Path.of("src/test/java/com/infnet/artigoEventos/httpReq/sample" + imageNumber + ".png");
        return new MockMultipartFile(
                "file",
                "sample1.png",
                "image/png",
                Files.readAllBytes(imagePath)
        );
    }

    private EventoCreateDto buildEventoDto(String nome) {
        EventoCreateDto dto = new EventoCreateDto();
        dto.setNome(nome);
        dto.setDescricao("Descricao Teste");
        dto.setLocalEvento("Local Teste");
        dto.setDataEvento(LocalDate.now().atStartOfDay());
        return dto;
    }

    private ParticipanteDto buildParticipante(String nome, String email) {
        ParticipanteDto dto = new ParticipanteDto();
        dto.setNome(nome);
        dto.setEmail(email);
        return dto;
    }

    private Usuario buildUsuario(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("123456");
        return u;
    }

    @BeforeEach
    void setup() throws IOException {
        participanteRepository.deleteAll();
        eventoRepository.deleteAll();
        usuarioRepository.deleteAll();

        sampleImage = loadSampleImage(1);

        organizador = buildUsuario("Organizador Teste", "organizador@test.com");
        usuarioRepository.save(organizador);

        eventoDto = buildEventoDto("Evento Teste");
        eventoModel = eventoService.createEvento(eventoDto, sampleImage, organizador.getEmail());
    }

    @Test
    void createEventoTest() throws IOException {
        EventoCreateDto dto = buildEventoDto("Evento");

        Evento eventoCreated = eventoService.createEvento(dto, sampleImage, organizador.getEmail());

        assertNotNull(eventoCreated.getId());
        assertEquals("Evento", eventoCreated.getNome());
        assertEquals(StatusEvento.ATIVO, eventoCreated.getStatus());
    }

    @Test
    void createEventoOrganizadorNaoExisteTest() {
        assertThrows(EntityNotFoundException.class, () ->
                eventoService.createEvento(eventoDto, sampleImage, "naoExiste@test.com")
        );
    }

    @Test
    void getAllEventosTest() {
        List<Evento> eventos = eventoService.getAllEventos();
        assertEquals(1, eventos.size());
    }

    @Test
    void getEventoByIdTest() {
        Evento encontrado = eventoService.getEventoById(eventoModel.getId());
        assertEquals(eventoModel.getId(), encontrado.getId());
    }

    @Test
    void getEventoByIdNaoEncontradoTest() {
        assertThrows(EntityNotFoundException.class, () ->
                eventoService.getEventoById(-1)
        );
    }

    @Test
    void updateEventoTest() {
        EventoUpdateDto dto = new EventoUpdateDto();
        dto.setNome("Nome Atualizado");
        dto.setDescricao("Desc Atualizada");

        Evento atualizado = eventoService.updateEvento(eventoModel.getId(), dto);

        assertEquals("Nome Atualizado", atualizado.getNome());
        assertEquals("Desc Atualizada", atualizado.getDescricao());
    }

    @Test
    void updateEventoImagemTest() throws IOException {
        MockMultipartFile imagem = loadSampleImage(2);

        Evento atualizado = eventoService.updateEventoImagem(eventoModel.getId(), imagem);

        assertNotNull(atualizado.getImagemPath());

        Path savedPath = Path.of("uploads-test").resolve(atualizado.getImagemPath());
        assertTrue(Files.exists(savedPath));
    }

    @Test
    void deleteEventoTest() {
        assertDoesNotThrow(() ->
                eventoService.deleteEvento(eventoModel.getId())
        );

        assertFalse(eventoRepository.existsById(eventoModel.getId()));
    }

    @Test
    void deleteEventoNaoEncontradoTest() {
        assertThrows(EntityNotFoundException.class, () ->
                eventoService.deleteEvento(-1)
        );
    }

    @Test
    void getEventoImagemTest() throws IOException {
        MockMultipartFile imagemOriginal = loadSampleImage(1);

        byte[] bytesRetornados = eventoService.getEventoImagem(eventoModel.getId());

        assertNotNull(bytesRetornados);
        assertTrue(bytesRetornados.length > 0);

        byte[] bytesOriginais = imagemOriginal.getBytes();

        assertArrayEquals(bytesOriginais, bytesRetornados);
    }

    @Test
    void getEventoImagemNaoExisteTest() throws IOException {
        EventoCreateDto dto = buildEventoDto("Sem Imagem");
        Evento eventoSemImagem = eventoService.createEvento(dto, null, organizador.getEmail());

        assertThrows(EntityNotFoundException.class, () ->
                eventoService.getEventoImagem(eventoSemImagem.getId())
        );
    }

    @Test
    void addParticipanteTest() {
        ParticipanteDto dto = buildParticipante("Participante Teste", "participante@test.com");

        Participante participante = eventoService.addParticipante(eventoModel.getId(), dto, organizador.getEmail());

        assertNotNull(participante.getId());
    }

    @Test
    void addParticipanteJaExisteTest() {
        ParticipanteDto dto = buildParticipante("Participante", "test@test.com");

        eventoService.addParticipante(eventoModel.getId(), dto, organizador.getEmail());

        assertThrows(IllegalArgumentException.class, () ->
                eventoService.addParticipante(eventoModel.getId(), dto, organizador.getEmail())
        );
    }

    @Test
    void addParticipanteOrganizadorInvalidoTest() {
        Usuario outroUsuario = buildUsuario("Outro", "outro@test.com");
        usuarioRepository.save(outroUsuario);

        ParticipanteDto dto = buildParticipante("Teste", "teste@test.com");

        assertThrows(SecurityException.class, () ->
                eventoService.addParticipante(eventoModel.getId(), dto, outroUsuario.getEmail())
        );
    }

    @Test
    void removeParticipanteTest() {
        ParticipanteDto dto = buildParticipante("P1", "p1@test.com");
        Participante p = eventoService.addParticipante(eventoModel.getId(), dto, organizador.getEmail());

        assertDoesNotThrow(() ->
                eventoService.removeParticipante(eventoModel.getId(), p.getId(), organizador.getEmail())
        );
    }

    @Test
    void removeParticipanteOrganizadorInvalidoTest() {
        Usuario outro = buildUsuario("Outro", "outro@test.com");
        usuarioRepository.save(outro);

        ParticipanteDto dto = buildParticipante("P1", "p1@test.com");
        Participante p = eventoService.addParticipante(eventoModel.getId(), dto, organizador.getEmail());

        assertThrows(SecurityException.class, () ->
                eventoService.removeParticipante(eventoModel.getId(), p.getId(), outro.getEmail())
        );
    }

    @Test
    void removeParticipanteNaoEncontradoTest() {
        assertThrows(EntityNotFoundException.class, () ->
                eventoService.removeParticipante(eventoModel.getId(), -1, organizador.getEmail())
        );
    }

    @Test
    void removeParticipanteDeOutroEventoTest() throws IOException {
        EventoCreateDto dto2 = buildEventoDto("Outro Evento");
        Evento evento2 = eventoService.createEvento(dto2, null, organizador.getEmail());

        ParticipanteDto dtoP = buildParticipante("X", "x@test.com");
        Participante p = eventoService.addParticipante(eventoModel.getId(), dtoP, organizador.getEmail());

        assertThrows(IllegalArgumentException.class, () ->
                eventoService.removeParticipante(evento2.getId(), p.getId(), organizador.getEmail())
        );
    }
}
import static org.junit.jupiter.api.Assertions.*;

class EventoServiceTest {

}
