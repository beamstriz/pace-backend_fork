package com.agu.gestaoescalabackend.client.response;


import java.util.List;

import com.agu.gestaoescalabackend.dto.ProcessoJudicialDTO;

import lombok.Data;

@Data
public class TarefaLoteResponse{

    private String menssagem;
    private List<ProcessoJudicialDTO> processosNaoEncontrados;

}
