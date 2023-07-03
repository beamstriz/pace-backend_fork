package com.agu.gestaoescalabackend.repositories;

import com.agu.gestaoescalabackend.entities.Mutirao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MutiraoRepository extends JpaRepository<Mutirao, Long> {
	List<Mutirao> findAllByOrderByIdAsc();

	@Modifying
	@Query(
			value = "TRUNCATE TABLE tb_mutirao RESTART IDENTITY CASCADE;",
			nativeQuery = true
	)
	void truncateTable();


}
