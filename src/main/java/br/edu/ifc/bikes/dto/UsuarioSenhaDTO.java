package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioSenhaDTO(
        @NotBlank(message = "A senha atual é obrigatória")
        @Size(min=6, max=6, message = "A senha atual deve ter 6 caracteres")
        String senhaAtual,
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min=6, max=6, message = "A nova senha deve ter 6 caracteres")
        String novaSenha,
        @NotBlank(message = "A confirmação de senha é obrigatória")
        @Size(min=6, max=6, message = "A confirmação de senha deve ter 6 caracteres")
        String confirmaSenha
) { }
