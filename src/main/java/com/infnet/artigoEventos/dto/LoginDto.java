package com.infnet.artigoEventos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email precisa ser válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}