package com.agu.gestaoescalabackend.client.response;


import java.util.List;

import lombok.Data;

@Data
public class TarefaLoteResponse{

    private String menssagem;
    private List<String> processosNaoEncontrados;

}
