package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.dto.PautaOnlyDto;
import com.agu.gestaoescalabackend.dto.PautistaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static java.time.temporal.TemporalAdjusters.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PautaService {
	
	private PautaRepository pautaRepository;
	
	private PautistaRepository pautistaRepository;
	
	private MutiraoService mutiraoService;

	////////////////////////////////// SERVIÇOS ///////////////////////////////////

	@Transactional(readOnly = true)
	public List<Pauta> findAll() {
		return pautaRepository.findAllByOrderByIdAsc();

	}

	@Transactional
	public List<PautaDto> findAllByMutiraoId(long mutiraoId){

		return pautaRepository.findAllByMutiraoId(mutiraoId)
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());

	}

	/* @Transactional
	public List<PautaDto> findAllByPautistaId(long PautistaId){

		return pautaRepository.findAllByPautistaId(PautistaId)
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());

	} */

	@Transactional
	public List<Pauta> findAllByPautistaId(long PautistaId){
		return pautaRepository.findAllByPautistaId(PautistaId);
	}

	@Transactional
	public List<PautaOnlyDto> findAllPautaOnlyByPautistaId(long PautistaId){
		return pautaRepository.findAllByPautistaId(PautistaId)
				.stream()
				.map(Pauta::toPautaOnlyDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public boolean existsByPautistaAndData(PautistaDto pautista, LocalDate data){
		return pautaRepository.existsByPautistaAndData(pautista.toEntity(), data);
	}


	@Transactional(readOnly = true)
	public Page<Pauta> findByFilters(String hora, String vara, String sala, Long pautista, String dataInicial,
			String dataFinal, int page, int size) {
		Pageable pageable;
		if (page == 0 && size == 0) {
			pageable = Pageable.unpaged();
		} else {
			pageable = PageRequest.of(page, size);
		}
		Pautista pautistaResponse = null;
		Page<Pauta> pautas;
		if (pautista != null) {
			pautistaResponse = pautistaRepository.findById(pautista).orElse(null);
		}
		if (dataInicial != null && dataFinal != null) {
			LocalDate inicial = LocalDate.parse(dataInicial);
			LocalDate finall = LocalDate.parse(dataFinal);
			pautas = pautaRepository.findAllByHoraAndVaraAndSalaAndPautistaAndDataBetween(hora, vara, sala,
					pautistaResponse, inicial,
					finall,
					pageable);
			return pautas;
		} else {
			pautas = pautaRepository.findAllByHoraAndVaraAndSalaAndPautista(hora, vara, sala, pautistaResponse,
					pageable);
			return pautas;
		}

	}

	@Transactional(readOnly = true)
	public Long getTotalRows() {
		return pautaRepository.count();
	}

	@Transactional(readOnly = true)
	public List<Long> countMes() {
		List<Long> listaCount = new ArrayList<>();
		LocalDate dataInicial = LocalDate.now().with(firstDayOfYear());
		LocalDate dataFinal = LocalDate.now().with(firstDayOfYear()).with(lastDayOfMonth());
		for (int i = 0; i < 12; i++) {
			listaCount.add(pautaRepository.countByDataBetween(dataInicial, dataFinal));
			dataInicial = dataInicial.plusMonths(1);
			dataFinal = dataFinal.plusMonths(1).with(lastDayOfMonth());
		}
		return listaCount;
	}

	@Transactional(readOnly = true)
	public PautaDto findById(Long id) {

		return pautaRepository.findById(id)
				.map(Pauta::toDto)
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public Pauta findByProcesso(String processo, String data) {
		LocalDate data2 = LocalDate.parse(data);
		return pautaRepository.findByProcessoAndData(processo, data2).orElse(null);
	}

	@Transactional
	public List<PautaDto> saveAllGeracaoEscala(List<PautaDto> listaPautaDto) {
		List <Pauta> pautaListEntity = listaPautaDto.stream()
		.map(PautaDto::toEntity)
		.collect(Collectors.toList());

		return pautaRepository.saveAll(pautaListEntity)
		.stream()
		.map(Pauta::toDto)
		.collect(Collectors.toList());
	}

	@Transactional
	public List<PautaDto> saveAll(List<PautaDto> listaPautaDto) {

		Mutirao mutirao = mutiraoService.save(listaPautaDto).toEntity();
		for (PautaDto pautaDto : listaPautaDto) {		
			Pauta pauta = pautaDto.toEntity();
			pauta.setMutirao(mutirao);
			if (validarCriacao(pautaDto, pauta)) {
				pautaRepository.save(pauta);
			}else{
				mutiraoService.excluir(mutirao.getId());
				return null;
			}
		}
		
		
		return pautaRepository.findAllByMutiraoId(mutirao.getId())
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public PautaDto editar(Long pautaDeAudienciaId, PautaDto pautaDto) {

		Optional<Pauta> pautaOptional = pautaRepository.findById(pautaDeAudienciaId);

		if (pautaOptional.isEmpty())
			return null;

		Pauta pauta = pautaOptional.get().forUpdate(pautaDto);

		pauta = pautaRepository.save(pauta);
		return pauta.toDto();
	}

	@Transactional
	public void excluir(Long pautaDeAudienciaId) {
		if (pautaRepository.existsById(pautaDeAudienciaId)) {

			Optional<Pauta> pautaOptional = pautaRepository.findById(pautaDeAudienciaId);
			if (pautaOptional.isPresent()) {
				Integer quantidadeDePautas = pautaOptional.get().getMutirao().getQuantidaDePautas();
				if (quantidadeDePautas == 1) {
					mutiraoService.excluir(pautaOptional.get().getMutirao().getId());
				}
			}
			pautaRepository.deleteById(pautaDeAudienciaId);
		}
	}

	/*------------------------------------------------
	 METODOS DO MUTIRAO
	------------------------------------------------*/
	private boolean validarCriacao(PautaDto pautaDto, Pauta pauta) {
		// Instancia um objeto base para verificar se já existe um registro 'nome'
		// no banco igual ao do DTO
		try {
			boolean pautaExistente = pautaRepository.existsByProcessoAndDataAndHora(pautaDto.getProcesso(), pautaDto.getData(), pautaDto.getHora());
			if (pautaExistente == true) {
				return false;
			}else{
				return true;
			}
			
		} catch (Exception e) {
			System.err.println("e.getMessage()");
			System.err.println(e.getMessage());
			return false;
		}
		/*
		 * return (pautaExistente == null || pautaExistente.equals(pauta)
		 * || !(pautaExistente.getData().isEqual(pauta.getData())));
		 */
	} 
}
