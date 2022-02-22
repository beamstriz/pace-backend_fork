package com.agu.gestaoescalabackend.entities;

import com.agu.gestaoescalabackend.dto.MutiraoDTO;
import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.enums.StatusPauta;
import com.agu.gestaoescalabackend.util.Conversor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tb_mutirao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mutirao implements Serializable {
	private static final long serialVersionUID = 1L;

	// ATRIBUTOS DE IDENTIFICAÇÃO
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(value = EnumType.STRING)
	private StatusPauta statusPauta;

	// ATRIBUTOS DE REGISTRO
	private Integer quantidaDePautas;

	// ATRIBUTOS PADRÃO
	private String vara;
	@Column(name = "data_inicial")
	private LocalDate dataInicial;
	@Column(name = "data_final")
	private LocalDate dataFinal;



	/*------------------------------------------------
     METODOS DE CONVERSÃO
    ------------------------------------------------*/

	public MutiraoDTO toDto(){
		return Conversor.converter(this, MutiraoDTO.class);
	}

	/*------------------------------------------------
    METODOS DE CRUD
    ------------------------------------------------*/

	public Mutirao forSave(List<PautaDto> pautaDtoList){

		this.statusPauta = StatusPauta.SEM_ESCALA;
		this.vara = pautaDtoList.get(0).getVara();
		this.dataInicial = pautaDtoList.get(0).getData();
		this.dataFinal = pautaDtoList.get(pautaDtoList.size() - 1).getData();

		this.quantidaDePautas = 0;

		return this;
	}

	public Mutirao forUpdate(Long id){
		this.id = id;
		return this;
	}

}
