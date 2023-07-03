package com.agu.gestaoescalabackend.client.response;

import lombok.Data;

@Data
public class TarefaLoteResponse{

    private String dataHora;
    private String sala;
    private String processo;
    private String[] interessados;
    private String vara;

}
