package com.agu.gestaoescalabackend.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponsavelResponse {
    private String username;
    private String usernameCanonical;
    private String email;
    private String emailCanonical;
    private boolean enabled;
    private String salt;
    private String password;
    private LastLogin lastLogin;
    private List<String> roles;
    private int id;
    private String nome;
    private int nivelAcesso;
    private String assinaturaHTML;
    private boolean validado;
    private String configuracoes;

    // Getters e Setters

    @Data
    public static class LastLogin {
        private String date;
    }
}
