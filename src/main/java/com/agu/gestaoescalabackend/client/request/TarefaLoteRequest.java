package com.agu.gestaoescalabackend.client.request;

import com.agu.gestaoescalabackend.dto.LoginDTO;
import com.agu.gestaoescalabackend.dto.ProcessoJudicialDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class TarefaLoteRequest {
    @NotNull
    private LoginDTO login; //vem do front

    private String etiqueta; //vem do front
    @NotNull
    private int especieTarefa; //vem do front
    @NotNull
    private int setorResponsavel; //vem do front
    @NotNull
    private int usuarioResponsavel; //back

    @NotNull
    private List<ProcessoJudicialDTO> listaProcessosJudiciais;

}
