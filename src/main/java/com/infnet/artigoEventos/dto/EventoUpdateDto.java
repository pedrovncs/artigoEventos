package com.infnet.artigoEventos.dto;

import com.infnet.artigoEventos.model.StatusEvento;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoUpdateDto {
    private String nome;

    @FutureOrPresent(message = "Data do evento não pode ser no passado")
    private LocalDateTime dataEvento;

    private String localEvento;
    private String descricao;
    private StatusEvento status;
}