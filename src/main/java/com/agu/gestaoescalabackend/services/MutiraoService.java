/**
 * @author Carlos Eduardo
 * A classe <b>Mutirao Service</b> é utilizada para tratar das regras de negócio relacionadas à entidade <b>Mutirao</b>. 
*/
package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.MutiraoDTO;
import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.GrupoPautista;
import com.agu.gestaoescalabackend.enums.StatusPauta;
import com.agu.gestaoescalabackend.enums.StatusPautista;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Service
@AllArgsConstructor
public class MutiraoService {

	private MutiraoRepository mutiraoRepository;
	private PautaRepository pautaRepository;
	private PautistaRepository pautistaRepository;

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
		List<Pauta> pautaList = pautaRepository.findAllByMutiraoId(mutiraoId);
		List<Pautista> pautistaList = retornarListaDe(grupoPautista);
		Pauta ultimaPauta = pautaList.get(0);
		Pautista pautistaAtual = pegarPautistaDisponivel(pautistaList, ultimaPauta);

		// EFETUA AS OPERAÇÕES PARA CADA PAUTA
		for (Pauta pautaAtual : pautaList) {

			// VERIFICA SE A SALA, DIA OU TURNO MUDARAM
			if (ultimaPauta.temOMesmoPeriodo(pautaAtual)) {

				pautaAtual.setPautista(pautistaAtual);
				pautistaAtual.atualizarSaldo(1, pautaAtual);
			}
			else {
				// REORDENA OS PAUTISTAS POR SALDO
				Collections.sort(pautistaList);

				pautistaAtual = pegarPautistaDisponivel(pautistaList, pautaAtual);
				pautaAtual.setPautista(pautistaAtual);
				pautistaAtual.atualizarSaldo(1, pautaAtual);

				// ATUALIZA A ÚLTIMA PAUTA
				ultimaPauta = pautaAtual;
			}
		}

		// DEFINE O STATUS DO MUTIRAO E SALVA A PAUTA
		mutirao.get().setStatusPauta(StatusPauta.COM_ESCALA);

		return pautaRepository.saveAll(pautaList)
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
	}

	/*------------------------------------------------
	 METODOS DA ESCALA
	------------------------------------------------*/

	private List<Pautista> retornarListaDe(GrupoPautista grupoPautista) {
		if (grupoPautista.equals(GrupoPautista.PROCURADOR) || grupoPautista.equals(GrupoPautista.PREPOSTO))
			return pautistaRepository.findAllByGrupoPautistaAndStatusPautistaOrderBySaldoPesoAsc(grupoPautista, StatusPautista.ATIVO);
		return pautistaRepository.findAllByStatusPautistaOrderBySaldoPesoAsc(
				StatusPautista.ATIVO);
	}

	private Pautista pegarPautistaDisponivel(List<Pautista> pautistaList, Pauta pautaAtual) {

		// BUSCA POR UM PAUTISTA DISPONÍVEL E QUE NÃO TRABALHOU NO DIA ANTERIOR
		for (Pautista pautista : pautistaList) {
			if (pautista.estaDisponivel(pautaAtual.getData())){
				if (pautista.estaDisponivel(pautaAtual.getData().minusDays(1))){
					return pautista;
				}
			}
		}

		// BUSCA SOMENTE POR UM PAUTISTA DISPONÍVEL
		for (Pautista pautista : pautistaList) {
			if (pautista.estaDisponivel(pautaAtual.getData())) {
				return pautista;
			}
		}
		return null;
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
