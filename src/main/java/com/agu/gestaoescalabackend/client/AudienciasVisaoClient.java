package com.agu.gestaoescalabackend.client;

import java.util.List;

import com.agu.gestaoescalabackend.client.request.TarefaLoteRequest;
import com.agu.gestaoescalabackend.client.request.UsuarioResponsavelRequest;
import com.agu.gestaoescalabackend.client.response.TarefaLoteResponse;
import com.agu.gestaoescalabackend.client.response.UsuarioResponsavelResponse;
import com.agu.gestaoescalabackend.dto.InsertTarefasLoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@FeignClient(name="visao", url = "http://localhost:3002/pano")
public interface AudienciasVisaoClient{
    @GetMapping(value="/insertTarefasLote")
    public List<TarefaLoteResponse> insertTarefasLoteSapiens(@RequestBody TarefaLoteRequest tarefas);

    @PostMapping(value="/getUsuarioResponsavel")
    public UsuarioResponsavelResponse getUsuarioResponsavel(@RequestBody UsuarioResponsavelRequest infoUsuario);
    
}
