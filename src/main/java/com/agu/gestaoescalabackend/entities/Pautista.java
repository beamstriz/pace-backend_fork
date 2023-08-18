package com.agu.gestaoescalabackend.entities;

import com.agu.gestaoescalabackend.dto.PautistaDto;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.util.Conversor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tb_pautista")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(value= {"pautas"})
public class Pautista implements Serializable, Comparable<Pautista> {
    private static final long serialVersionUID = 1L;

    // ATRIBUTOS DE IDENTIFICAÇÃO
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idSapiens;

    @Column(unique = true)
    private String nome;
    @Enumerated(value = EnumType.STRING)
    private GrupoPautista grupoPautista;

    // ATRIBUTOS DE ESTADO
    @Enumerated(value = EnumType.STRING)
    private StatusPautista statusPautista;
    private LocalDate dataInicial;
    private LocalDate dataFinal;

    // ATRIBUTOS DE ESCALA
    @Formula("(select count(*) FROM tb_pauta s WHERE s.pautista_id = id)")
    private Integer saldo;
    private Integer peso;
    @Formula("(select count(*) FROM tb_pauta s WHERE s.pautista_id = id) * peso")
    private Integer saldoPeso;

    // ATRIBUTOS DE RELACIONAMENTO
    @OneToMany(mappedBy = "pautista")
    private List<Pauta> pautas;

    /*------------------------------------------------
     METODOS DE CONVERSÃO
    ------------------------------------------------*/

    public PautistaDto toDto(){
        return Conversor.converter(this, PautistaDto.class);
    }

    public PautistaDto toNotListPautaDto(){
        return new PautistaDto(
            id, nome, grupoPautista, statusPautista, dataInicial,dataFinal,saldo,peso,saldoPeso
        );
    }

    /*------------------------------------------------
    METODOS DE CRUD
    ------------------------------------------------*/

    public Pautista forSave(){
        this.saldoPeso = 0;
        return this;
    }

    public Pautista forUpdate(PautistaDto pautistaDto){
        this.nome = pautistaDto.getNome();
        this.grupoPautista = pautistaDto.getGrupoPautista();
        this.statusPautista = pautistaDto.getStatusPautista();
        this.dataInicial = pautistaDto.getDataInicial();
        this.dataFinal = pautistaDto.getDataFinal();

        this.peso = pautistaDto.getPeso();

        return this;
    }

    /*------------------------------------------------
    METODOS DE NEGÓCIO
    ------------------------------------------------*/

    @Override
    public int compareTo(Pautista outroPautista) {
        if (this.saldoPeso < outroPautista.saldoPeso) {
            return -1;
        } else if (this.saldoPeso > outroPautista.saldoPeso) {
            return 1;
        }
        return 0;
    }

    public void atualizarSaldo(int valor, Pauta pauta){
        this.setSaldo(this.getSaldo() + valor);
        this.setSaldoPeso(this.getSaldo() * this.getPeso());
        /* this.pautas.add(pauta); */
    }

}
