package com.agu.gestaoescalabackend.dto;

import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.services.PautaService;
import com.agu.gestaoescalabackend.util.Conversor;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PautistaDto implements Serializable, Comparable<PautistaDto> {
    private static final long serialVersionUID = 1L;

    @Autowired
    private PautaService pautaService;
    // ATRIBUTOS DE IDENTIFICAÇÃO
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank
    private String nome;
    @NotNull
    @JsonProperty(value = "grupo")
    private GrupoPautista grupoPautista;

    // ATRIBUTOS DE ESTADO
    @NotNull
    @JsonProperty(value = "status")
    private StatusPautista statusPautista;
    private LocalDate dataInicial;
    private LocalDate dataFinal;

    // ATRIBUTOS DE ESCALA
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer saldo;
    @NotNull
    private Integer peso;
    private Integer saldoPeso;

    // ATRIBUTOS DE RELACIONAMENTO
    private List<PautaOnlyDto> pautas = new ArrayList<>();

    /*------------------------------------------------
     METODOS DE CONVERSÃO
    ------------------------------------------------*/

    public Pautista toEntity(){
        return Conversor.converter(this, Pautista.class);
    }

    public void atualizarSaldo(int valor, PautaOnlyDto pauta){
        this.setSaldo(this.getSaldo() + valor);
        this.setSaldoPeso(this.getSaldo() * this.getPeso());
        this.pautas.add(pauta);
    }

    @Override
    public int compareTo(PautistaDto outroPautista) {
        if (this.getSaldo() < outroPautista.getSaldo()) {
            return -1;
        }else if(this.getSaldo() > outroPautista.getSaldo()){
            return 1;
        }else{
            return 0;
        }
        
    }

    /* ----------------------------------------
        MÉTODOS CONSTRUTORES 
    ------------------------------------------*/

    public PautistaDto(Long id, String nome,GrupoPautista grupoPautista,StatusPautista statusPautista,LocalDate dataInicial, LocalDate dataFinal, Integer saldo, Integer peso, Integer saldoPeso){

        this.id = id;
        this.nome = nome;
        this.grupoPautista = grupoPautista;
        this.statusPautista = statusPautista;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.saldo = saldo;
        this.peso = peso;
        this.saldoPeso = saldoPeso;
    }

    
}
