package com.infnet.artigoEventos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParticipanteDto {
    @NotBlank(message = "Nome do participante é obrigatório")
    private String nome;

    @NotBlank(message = "Email do participante é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
}