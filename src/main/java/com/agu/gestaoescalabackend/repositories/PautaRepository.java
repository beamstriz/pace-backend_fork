package com.agu.gestaoescalabackend.repositories;

import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.entities.Pautista;
import com.agu.gestaoescalabackend.enums.TipoPauta;
import com.agu.gestaoescalabackend.enums.TurnoPauta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {

	Pauta findByProcessoAndTipoPauta(String processo, TipoPauta tipoPauta);

	List<Pauta> findByDataAndSalaAndTurnoPautaAndVara(LocalDate data, String sala, TurnoPauta turno, String vara);

	List<Pauta> findAllByMutiraoId(Long mutirao_id);

	List<Pauta> findAllByPautistaId(long PautistaId);

	boolean existsByPautistaAndData(Pautista pautista, LocalDate data);

	List<Pauta> findAllByOrderByIdAsc();

	@Query("SELECT pauta FROM Pauta pauta WHERE (:hora is null or pauta.hora = :hora) AND (:vara is null or pauta.vara = :vara) AND (:sala is null or pauta.sala = :sala) AND (:pautista is null or pauta.pautista = :pautista) AND pauta.pautista is not null AND (pauta.data BETWEEN :dataInicial AND :dataFinal) Order by pauta.data ASC, pauta.hora ASC")
	Page<Pauta> findAllByHoraAndVaraAndSalaAndPautistaAndDataBetween(@Param("hora") String hora, @Param("vara")String vara, @Param("sala") String sala,
	@Param("pautista") Pautista pautista, @Param("dataInicial") LocalDate dataInicial, @Param("dataFinal") LocalDate dataFinal, Pageable pageable);

	@Query("SELECT pauta FROM Pauta pauta WHERE (:hora is null or pauta.hora = :hora) AND (:vara is null or pauta.vara = :vara) AND (:sala is null or pauta.sala = :sala) AND (:pautista is null or pauta.pautista = :pautista) AND pauta.pautista is not null ORDER BY pauta.data ASC, pauta.hora ASC")
	Page<Pauta> findAllByHoraAndVaraAndSalaAndPautista(@Param("hora") String hora,@Param("vara") String vara, @Param("sala") String sala, @Param("pautista") Pautista pautista,
			Pageable pageable);

	Long countByDataBetween(LocalDate dataInicial, LocalDate dataFinal);

	Optional<Pauta> findByProcessoAndData(String processo, LocalDate data);

	List<Pauta> findByProcessoAndDataAndHora(String processo, LocalDate data, String hora);

	boolean existsByProcessoAndDataAndHora(String processo, LocalDate data, String hora);

	@Modifying
	@Query(value = "TRUNCATE TABLE tb_pauta RESTART IDENTITY CASCADE;", nativeQuery = true)
	void truncateTable();
}
