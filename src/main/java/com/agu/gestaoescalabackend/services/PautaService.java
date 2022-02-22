package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.MutiraoDTO;
import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.enums.StatusPauta;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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

//////////////////////////////////   SERVIÇOS   ///////////////////////////////////

	@Transactional(readOnly = true)
	public List<PautaDto> findAll() {

		return pautaRepository.findAllByOrderByIdAsc()
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public PautaDto findById(Long id) {

		return pautaRepository.findById(id)
				.map(Pauta::toDto)
				.orElse(null);
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

		List<Mutirao> mutirao = mutiraoRepository.findByVara(pautaDto.getVara());

		Optional<Pauta> pautaOptional = pautaRepository.findById(pautaDeAudienciaId);

		if (pautaOptional.isEmpty())
			return null;
		
		Pauta pauta = pautaOptional.get().forUpdate(pautaDto);
		inserirMutirao(mutirao, pauta);

		pauta = pautaRepository.save(pauta);
		return pauta.toDto();
	}

	@Transactional
	public void excluir(Long pautaDeAudienciaId) {
		if (pautaRepository.existsById(pautaDeAudienciaId))
			pautaRepository.deleteById(pautaDeAudienciaId);
	}

	/*------------------------------------------------
     METODOS DO MUTIRAO
    ------------------------------------------------*/

	public void inserirMutirao(List<Mutirao> mutirao,
							   Pauta pauta) {
		int x = 0;
		// Faça enquanto estiver dentro do tamanho do multirão (OU) enquanto o multirão
		// for nulo
		while ((x < mutirao.size()) || (mutirao == null)) {

			LocalDate dataInicialMutirao = mutirao.get(x).getDataInicial().minusDays(1);
			LocalDate dataFinalMutirao = mutirao.get(x).getDataFinal().plusDays(1);

			if (dataInicialMutirao.isBefore(pauta.getData())
					&& dataFinalMutirao.isAfter(pauta.getData())) {
				// Se a condição das datas for verdadeira, seta na pauta o multirão corrente
				pauta.setMutirao(mutirao.get(x));
				break;

			}
			x++;
		}
	}

	private boolean validarCriacao(PautaDto pautaDto, Pauta pauta) {
		// Instancia um objeto base para verificar se já existe um registro 'nome'
		// no banco igual ao do DTO
		Pauta pautaExistente = pautaRepository.findByProcessoAndTipoPauta(pautaDto.getProcesso(),
				pautaDto.getTipoPauta());
		return (pautaExistente == null || pautaExistente.equals(pauta));
	}
}
