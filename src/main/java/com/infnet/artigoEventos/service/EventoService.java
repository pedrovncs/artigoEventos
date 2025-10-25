package com.infnet.artigoEventos.service;

import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.*;
import com.infnet.artigoEventos.repository.EventoRepository;
import com.infnet.artigoEventos.repository.ParticipanteRepository;
import com.infnet.artigoEventos.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ParticipanteRepository participanteRepository;

    private final Path rootLocation;
    @Autowired
    public EventoService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possivel iniciar o armazenamento de arquivos.", e);
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID().toString() + "_" + originalFilename;

        Path destinationFile = this.rootLocation.resolve(filename).toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return filename;
    }

    private void deleteFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        try {
            Path file = this.rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Falha ao deletar arquivo: " + filename);
        }
    }

    public List<Evento> getAllEventos() {
        return eventoRepository.findAll();
    }

    public Evento getEventoById(Integer id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento nao encontrado com id: " + id));
    }

    public Evento createEvento(EventoCreateDto dto, MultipartFile imagem) throws IOException {
        Usuario organizador = usuarioRepository.findById(dto.getOrganizadorId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario Organizador nao encotrado com id: " + dto.getOrganizadorId()));

        Evento evento = new Evento();
        evento.setNome(dto.getNome());
        evento.setDataEvento(dto.getDataEvento());
        evento.setLocalEvento(dto.getLocalEvento());
        evento.setDescricao(dto.getDescricao());
        evento.setOrganizador(organizador);
        evento.setStatus(StatusEvento.ATIVO);

        if (imagem != null && !imagem.isEmpty()) {
            String filename = saveFile(imagem);
            evento.setImagemPath(filename);
        }
        return eventoRepository.save(evento);
    }

    public Participante addParticipante(Integer eventoId, ParticipanteDto dto) {
        Evento evento = getEventoById(eventoId);
        if (participanteRepository.existsByEmailAndEvento(dto.getEmail(), evento)) {
            throw new IllegalArgumentException("Este email ja esta convidado para o evento");
        }
        Participante participante = new Participante();
        participante.setNome(dto.getNome());
        participante.setEmail(dto.getEmail());
        participante.setEvento(evento);
        return participanteRepository.save(participante);
    }


    public Evento updateEvento(Integer id, EventoUpdateDto dto) {
        Evento evento = getEventoById(id);

        if (dto.getNome() != null) evento.setNome(dto.getNome());
        if (dto.getDataEvento() != null) evento.setDataEvento(dto.getDataEvento());
        if (dto.getLocalEvento() != null) evento.setLocalEvento(dto.getLocalEvento());
        if (dto.getDescricao() != null) evento.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null) evento.setStatus(dto.getStatus());

        return eventoRepository.save(evento);
    }

    public Evento updateEventoImagem(Integer id, MultipartFile imagem) throws IOException {
        Evento evento = getEventoById(id);

        deleteFile(evento.getImagemPath());

        String newFilename = saveFile(imagem);
        evento.setImagemPath(newFilename);

        return eventoRepository.save(evento);
    }

    public void deleteEvento(Integer id) {
        Evento evento = getEventoById(id);
        deleteFile(evento.getImagemPath());

        eventoRepository.deleteById(id);
    }

    public byte[] getEventoImagem(Integer id) throws IOException {
        Evento evento = getEventoById(id);
        String filename = evento.getImagemPath();

        if (filename == null || filename.isEmpty()) {
            throw new EntityNotFoundException("Imagem nao encotrado no evento de id: " + id);
        }

        Path file = this.rootLocation.resolve(filename);
        if (!Files.exists(file) || !Files.isReadable(file)) {
            throw new EntityNotFoundException("Imagem nao encontrada: " + filename);
        }

        return Files.readAllBytes(file);
    }

    public void removeParticipante(Integer eventoId, Integer participanteId) {
        Evento evento = getEventoById(eventoId);

        Participante participante = participanteRepository.findById(participanteId)
                .orElseThrow(() -> new EntityNotFoundException("Participante nao encontrado comid" + participanteId));

        if (!participante.getEvento().getId().equals(evento.getId())) {
            throw new IllegalArgumentException("Participante nao convidado para este evento");
        }

        participanteRepository.delete(participante);
    }
}