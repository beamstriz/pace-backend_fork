package com.agu.gestaoescalabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginDTO {
    private String cpf;
    private String senha;
}
