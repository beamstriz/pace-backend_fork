package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;
import com.agu.gestaoescalabackend.util.PageResponse;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PautaService {

	private PautaRepository pautaRepository;
	private PautistaRepository pautistaRepository;
	private MutiraoRepository mutiraoRepository;
	private MutiraoService mutiraoService;

	////////////////////////////////// SERVIÇOS ///////////////////////////////////

	@Transactional(readOnly = true)
	public List<PautaDto> findAll() {
		return pautaRepository.findAllByOrderByIdAsc()
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
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
		//Long maxElements;
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
		//	maxElements = pautas.getTotalElements();
		//	return new PageResponse(pautas.stream().map(Pauta::toDto).collect(Collectors.toList()), maxElements);
		} else {
			pautas = pautaRepository.findAllByHoraAndVaraAndSalaAndPautista(hora, vara, sala, pautistaResponse,
					pageable);
					return pautas;
		//	maxElements = pautas.getTotalElements();
		//	return new PageResponse(pautas.stream().map(Pauta::toDto).collect(Collectors.toList()), maxElements);
		}

	}

	@Transactional(readOnly = true)
	public Long getTotalRows() {
		return pautaRepository.count();
	}

	@Transactional(readOnly = true)
	public PautaDto findById(Long id) {

		return pautaRepository.findById(id)
				.map(Pauta::toDto)
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public PautaDto findByProcesso(String processo) {
		return pautaRepository.findByProcesso(processo).map(Pauta::toDto).orElse(null);
	}

	@Transactional
	public List<PautaDto> save(List<PautaDto> listaPautaDto) {

		Mutirao mutirao = mutiraoService.save(listaPautaDto).toEntity();

		for (PautaDto pautaDto : listaPautaDto) {

			Pauta pauta = pautaDto.toEntity();
			pauta.setMutirao(mutirao);

			if (validarCriacao(pautaDto, pauta)) {
				pautaRepository.save(pauta).toDto();
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
					mutiraoRepository.deleteById(pautaOptional.get().getMutirao().getId());
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
		Pauta pautaExistente = pautaRepository.findByProcessoAndTipoPauta(pautaDto.getProcesso(),
				pautaDto.getTipoPauta());
		return (pautaExistente == null || pautaExistente.equals(pauta) || !(pautaExistente.getData().isEqual(pauta.getData())));
	}
}
