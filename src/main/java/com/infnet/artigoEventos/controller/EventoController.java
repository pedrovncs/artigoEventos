package com.infnet.artigoEventos.controller;

import com.infnet.artigoEventos.dto.EventoCreateDto;
import com.infnet.artigoEventos.dto.EventoUpdateDto;
import com.infnet.artigoEventos.dto.ParticipanteDto;
import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import com.infnet.artigoEventos.service.EventoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<Evento>> getAllEventos() {
        return ResponseEntity.ok(eventoService.getAllEventos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventoById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(eventoService.getEventoById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvento(
            @Valid @RequestPart("evento") EventoCreateDto eventoDto,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            Authentication authentication) {
        try {
            String organizadorEmail = authentication.getName();

            Evento novoEvento = eventoService.createEvento(eventoDto, imagem, organizadorEmail);
            return ResponseEntity.status(201).body(novoEvento);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro processando a imagem");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvento(@PathVariable Integer id, @Valid @RequestBody EventoUpdateDto dto) {
        try {
            Evento evento = eventoService.updateEvento(id, dto);
            return ResponseEntity.ok(evento);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateEventoImagem(
            @PathVariable Integer id,
            @RequestPart("imagem") MultipartFile imagem) {
        try {
            Evento evento = eventoService.updateEventoImagem(id, imagem);
            return ResponseEntity.ok(evento);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro processando a imagem");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvento(@PathVariable Integer id) {
        try {
            eventoService.deleteEvento(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/{id}/participantes")
    public ResponseEntity<?> addParticipante(
            @PathVariable("id") Integer eventoId,
            @Valid @RequestBody ParticipanteDto participanteDto,
            Authentication authentication) {
        try {
            String requisitanteEmail = authentication.getName();
            Participante novoParticipante = eventoService.addParticipante(eventoId, participanteDto, requisitanteEmail);
            return ResponseEntity.status(201).body(novoParticipante);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/participantes/{participanteId}")
    public ResponseEntity<?> removeParticipante(
            @PathVariable("id") Integer eventoId,
            @PathVariable("participanteId") Integer participanteId,
            Authentication authentication) {
        try {
            String requisitanteEmail = authentication.getName();

            eventoService.removeParticipante(eventoId, participanteId, requisitanteEmail);

            return ResponseEntity.noContent().build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}