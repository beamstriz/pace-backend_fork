package com.agu.gestaoescalabackend.util;

import java.util.List;

import com.agu.gestaoescalabackend.dto.PautaDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResponse {

    private List<PautaDto> pautas;
    private Long maxElements;

    public PageResponse(List<PautaDto> pautas, Long maxElements) {
        this.pautas = pautas;
        this.maxElements = maxElements;
    }

}
