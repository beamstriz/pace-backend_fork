package com.agu.gestaoescalabackend.client.request;

import com.agu.gestaoescalabackend.dto.LoginDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UsuarioResponsavelRequest {
    private LoginDTO login;

    private String query;

    private int setorResponsavel;

}
