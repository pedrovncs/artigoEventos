package com.infnet.artigoEventos.dto;

import lombok.Data;

@Data
public class UsuarioUpdateDto {
    private String nome;

    @jakarta.validation.constraints.Size(min = 6, message = "Senha deve ter no minio 6 caracteres")
    private String senha;
}