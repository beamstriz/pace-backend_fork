/**
 * @author Carlos Eduardo
 * A classe <b>Mutirao Service</b> é utilizada para tratar das regras de negócio relacionadas à entidade <b>Mutirao</b>. 
*/
package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.MutiraoDTO;
import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.dto.PautistaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPauta;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@Service
public class MutiraoService {
	
	@Autowired
	private MutiraoRepository mutiraoRepository;
	
	@Autowired
	private PautaRepository pautaRepository;

	@Autowired
	private PautaService pautaService;
		
	@Autowired
	private PautistaRepository pautistaRepository;

	@Autowired
	private PautistaService pautistaService;

	@Transactional(readOnly = true)
	public List<MutiraoDTO> findAll() {

		return mutiraoRepository.findAllByOrderByIdAsc()
				.stream()
				.map(Mutirao::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public MutiraoDTO findById(Long id) {

		return mutiraoRepository.findById(id)
				.map(Mutirao::toDto)
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public List<PautaDto> findPautas(Long mutiraoId) {

		return  pautaRepository.findAllByMutiraoId(mutiraoId)
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public MutiraoDTO save(List<PautaDto> pautaDtoList) {

		Mutirao mutirao = new Mutirao();
		mutirao.forSave(pautaDtoList);

		return mutiraoRepository.save(mutirao).toDto();
	}

	@Transactional
	public MutiraoDTO update(Long mutiraoId, MutiraoDTO mutiraoDto) {

		if (!mutiraoRepository.existsById(mutiraoId))
			return null;

		atualizarVaraPautas(mutiraoId, mutiraoDto.getVara());

		Mutirao mutirao = mutiraoDto.toEntity().forUpdate(mutiraoId);
		return mutiraoRepository.save(mutirao).toDto();
	}

	@Transactional
	public void excluir(Long mutiraoId) {
		if (mutiraoRepository.existsById(mutiraoId)){
			Optional<Mutirao> mutirao = mutiraoRepository.findById(mutiraoId);
			if (mutirao.isPresent()){
				List<Pauta> pautaList = pautaRepository.findAllByMutiraoId(mutiraoId);
				pautaRepository.deleteAll(pautaList);
			}
			mutiraoRepository.deleteById(mutiraoId);
		}
	}

  	/*------------------------------------------------
    ESCALA
    ------------------------------------------------*/

	@Transactional
	public List<PautaDto> gerarEscala(Long mutiraoId, GrupoPautista grupoPautista) { // 24 linhas

		// VERIFICA SE O MUTIRÃO EXISTE OU SE JÁ ESTÁ COM ESCALA
		Optional<Mutirao> mutirao = mutiraoRepository.findById(mutiraoId);
		if (!mutirao.isPresent()
				|| mutirao.get().getStatusPauta().equals(StatusPauta.COM_ESCALA)){
			return null;}

		// INSTANCIA A LISTA DE OBJETOS
		List<PautaDto> pautaList = pautaService.findAllByMutiraoId(mutiraoId);
		List<PautistaDto> pautistaList = retornarListaDe(grupoPautista);
		/* PautaDto ultimaPauta = pautaList.get(pautaList.size()-1); */
		PautaDto ultimaPauta = pautaList.get(0);
		PautistaDto pautistaAtual = pegarPautistaDisponivel(pautistaList,ultimaPauta);
		// EFETUA AS OPERAÇÕES PARA CADA PAUTA
		
		for (PautaDto pautaAtual : pautaList) {

			// VERIFICA SE A SALA, DIA OU TURNO MUDARAM
			if (ultimaPauta.temOMesmoPeriodo(pautaAtual)) {

				pautaAtual.setPautista(pautistaAtual);
				pautistaAtual.atualizarSaldo(1,pautaAtual.toPautaOnlyDto());
	
			}
			else {
				// REORDENA OS PAUTISTAS POR SALDO
				Collections.sort(pautistaList);
				pautistaAtual = pegarPautistaDisponivel(pautistaList, pautaAtual);
				pautaAtual.setPautista(pautistaAtual);
				pautistaAtual.atualizarSaldo(1,pautaAtual.toPautaOnlyDto());

				// ATUALIZA A ÚLTIMA PAUTA
				ultimaPauta = pautaAtual;
			}
			
		}

		// DEFINE O STATUS DO MUTIRAO E SALVA A PAUTA
		mutirao.get().setStatusPauta(StatusPauta.COM_ESCALA);

		return pautaService.saveAllGeracaoEscala(pautaList);
				
	}

	/*------------------------------------------------
	 METODOS DA ESCALA
	------------------------------------------------*/

	private List<PautistaDto> retornarListaDe(GrupoPautista grupoPautista) {
		if (grupoPautista.equals(GrupoPautista.PROCURADOR) || grupoPautista.equals(GrupoPautista.PREPOSTO)){
			return pautistaService.findAllByGrupoPautistaAndStatusPautistaOrderBySaldoPesoAsc(grupoPautista, StatusPautista.ATIVO);
		}
			return pautistaService.findAllByStatusPautistaOrderBySaldoPesoAsc(
				StatusPautista.ATIVO);
	}

	private PautistaDto pegarPautistaDisponivel(List<PautistaDto> pautistaList, PautaDto pautaAtual) {
		
		PautistaDto pautistaDisponivel = new PautistaDto();

		for (PautistaDto pautista : pautistaList) {
			// BUSCA POR UM PAUTISTA DISPONÍVEL E QUE NÃO TRABALHOU NO DIA ANTERIOR
			if (pautistaService.estaDisponivel(pautista,pautaAtual.getData()) && pautistaService.estaDisponivel(pautista,pautaAtual.getData().minusDays(1))){
				return pautista;
				
			// BUSCA SOMENTE POR UM PAUTISTA DISPONÍVEL
			}else if (pautistaService.estaDisponivel(pautista,pautaAtual.getData())){
				pautistaDisponivel = pautista;
			}
			
		}

		return pautistaDisponivel;
		
	}

	/*------------------------------------------------
	 METODOS DE NEGÓCIO
	------------------------------------------------*/

	@Transactional
	public List<Pauta> atualizarProcurador(Long pautaDeAudienciaId, Long procuradorId) {

		if ((!pautaRepository.existsById(pautaDeAudienciaId)) || (!pautistaRepository.existsById(procuradorId)))
			return null;

		Pauta pautaDoPautista = pautaRepository.findById(pautaDeAudienciaId).get();
		List<Pauta> listaPautaDoPautista =
				pautaRepository.findByDataAndSalaAndTurnoPautaAndVara(pautaDoPautista.getData(), pautaDoPautista.getSala(),
						pautaDoPautista.getTurnoPauta(), pautaDoPautista.getVara());

		Pautista pautistaNovo = pautistaRepository.findById(procuradorId).get();

		for (Pauta pauta : listaPautaDoPautista) {
			pauta.setPautista(pautistaNovo);
			pautaRepository.save(pauta);
		}

		return listaPautaDoPautista;
	}

	private void atualizarVaraPautas(Long mutiraoId, String vara) {

		if (mutiraoRepository.findById(mutiraoId).get().getVara() != vara) {
			List<Pauta> pauta = pautaRepository.findAllByMutiraoId(mutiraoId);
			pauta.forEach(x -> x.setVara(vara));
		}

	}

	/*------------------------------------------------
	 METODOS DE COMENTÁRIOS
	------------------------------------------------*/

//	private void ExibirListaPautista(List<Pautista> pautistaList) {
//		Titulo("Exibir Lista Ordenada");
//		pautistaList
//				.forEach(
//						pautista -> {
//							System.out.println(pautista.getSaldo() + " | " + pautista.getNome());
//						}
//				);
//	}
//	private void Titulo(String s) {
//		System.out.println();
//		System.out.println("----- " + s);
//		System.out.println();
//	}
//
//	private Pautista pegarPautistaDisponivel(List<Pautista> pautistaList, Pauta pautaAtual) {
//
//		Titulo("Buscando Pautista");
//		System.out.println("- Data "+pautaAtual.getData());
//		System.out.println("- Sala "+pautaAtual.getSala());
//		System.out.println("- Turno "+pautaAtual.getTurnoPauta());
//		System.out.println("- Vara "+pautaAtual.getVara());
//		System.out.println();
//
//		// BUSCA POR UM PAUTISTA DISPONÍVEL E QUE NÃO TRABALHOU NO DIA ANTERIOR
//		for (Pautista pautista : pautistaList) {
//			System.out.println();
//			System.out.println(pautista.getNome());
//			pautista.getPautas().forEach(
//					pauta -> {
//						System.out.println("  "+pauta.getData());
//					}
//			);
//
//			if (pautista.estaDisponivel(pautaAtual.getData())){
//				System.out.println("    - Está livre");
//				if (pautista.estaDisponivel(pautaAtual.getData().minusDays(1))){
//					System.out.println("    - Está com folga");
//					return pautista;
//				} else {
//					System.out.println("    - Não está com folga");
//				}
//			}else{
//				System.out.println("    - Ocupado");
//			}
//		}
//
//		System.out.println();
//		System.out.println("!BUSCA PESADA!");
//
//		// BUSCA SOMENTE POR UM PAUTISTA DISPONÍVEL
//		for (Pautista pautista : pautistaList) {
//			System.out.println();
//			System.out.println(pautista.getNome());
//			pautista.getPautas().forEach(
//					pauta -> {
//						System.out.println("  "+pauta.getData());
//					}
//			);
//
//			System.out.println(" Pautista "+pautista.getNome());
//			if (pautista.estaDisponivel(pautaAtual.getData())) {
//				System.out.println("    - Está livre");
//				return pautista;
//			} else {
//				System.out.println("    - Ocupado");
//			}
//		}
//		return null;
//	}
	

}
