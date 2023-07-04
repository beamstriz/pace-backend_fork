package com.agu.gestaoescalabackend.client.request;

import com.agu.gestaoescalabackend.dto.FiltroDTO;
import com.agu.gestaoescalabackend.dto.LoginDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TarefaLoteRequest {
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
