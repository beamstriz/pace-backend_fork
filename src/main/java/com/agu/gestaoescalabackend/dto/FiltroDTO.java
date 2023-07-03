package com.agu.gestaoescalabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiltroDTO {
    private String dataInicial;
    private String dataFinal;
    private String hora;
    private String vara;
    private String sala;
    private Long pautista;
    private int page;
    private int size;
}
