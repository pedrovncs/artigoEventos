package com.infnet.artigoEventos.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoCreateDto {
    @NotBlank(message = "Nome do evento é obrigatório")
    private String nome;

    @NotNull(message = "Data do evento é obrigatória")
    @FutureOrPresent(message = "A data do evento não pode ser no passado")
    private LocalDateTime dataEvento;

    @NotBlank(message = "O local do evento é obrigatório")
    private String localEvento;

    private String descricao;
}