package com.agu.gestaoescalabackend.client;

import java.util.List;

import com.agu.gestaoescalabackend.client.response.TarefaLoteResponse;
import com.agu.gestaoescalabackend.dto.InsertTarefasLoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient(name="visao", url = "http://localhost:3002/pano")
public interface AudienciasVisaoClient{
    @GetMapping(value="/insertTarefasLote")
    public List<TarefaLoteResponse> insertTarefasLoteSapiens(@RequestBody InsertTarefasLoteDTO tarefas);
    
}
