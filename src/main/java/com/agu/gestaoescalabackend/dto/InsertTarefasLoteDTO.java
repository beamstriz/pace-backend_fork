package com.agu.gestaoescalabackend.dto;
import com.agu.gestaoescalabackend.dto.LoginDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
public class InsertTarefasLoteDTO {
    @NotNull
    private LoginDTO login;

    private String etiqueta;
    @NotNull
    private int especieTarefa;
    @NotNull
    private int setorResponsavel;
    @NotNull
    private int usuarioResponsavel;
    @NotNull
    private int setorOrigem;
    @NotNull
    private String prazoInicio;
    @NotNull
    private String prazoFim;
    @NotNull
    private String[] listaProcessosJudiciais;
}
