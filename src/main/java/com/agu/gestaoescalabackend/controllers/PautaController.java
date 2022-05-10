package com.agu.gestaoescalabackend.controllers;

import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.services.PautaService;

import lombok.AllArgsConstructor;

import org.apache.catalina.connector.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/pauta")
@AllArgsConstructor
public class PautaController {

	private PautaService pautaService;
	private PautaRepository pautaRepository;

	@GetMapping
	public ResponseEntity<List<Pauta>> findAll() {
		List<Pauta> response = pautaService.findAll();
		return ResponseEntity.ok(response);
	}

	@GetMapping("/filtro")
	public ResponseEntity<Page<Pauta>> findByFilters(@RequestParam(required = false) String hora,
			@RequestParam(required = false) String vara,
			@RequestParam(required = false) String sala, @RequestParam(required = false) Long pautista,
			@RequestParam(required = false) String dataInicial,
			@RequestParam(required = false) String dataFinal, @RequestParam int page, @RequestParam int size) {
		Page<Pauta> response = pautaService.findByFilters(hora, vara, sala, pautista, dataInicial, dataFinal, page,
				size);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/total")
	public ResponseEntity<Void> getTotalRows() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Expose-Headers", "maxElements");
		headers.add("maxElements", Long.toString(pautaService.getTotalRows()));
		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	@GetMapping("/processo")
	public ResponseEntity<Pauta> findByProcesso(@RequestParam String processo, @RequestParam String data) {
		Pauta pauta = pautaService.findByProcesso(processo, data);
		return ResponseEntity.ok(pauta);
	}

	@GetMapping("/{pautaDeAudienciaId}")
	public ResponseEntity<PautaDto> findById(@PathVariable Long pautaDeAudienciaId) {
		PautaDto pautaDto = pautaService.findById(pautaDeAudienciaId);
		return ResponseEntity.ok(pautaDto);
	}

	@GetMapping("/mes")
	public ResponseEntity<List<Long>> countMes() {
		return ResponseEntity.ok(pautaService.countMes());
	}

	@PostMapping
	public ResponseEntity<List<PautaDto>> save(@RequestBody List<PautaDto> PautaDto) {
		List<PautaDto> listaPautaDto = pautaService.save(PautaDto);
		if (listaPautaDto == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		return ResponseEntity.ok(listaPautaDto);
	}

	@PutMapping("/{pautaDeAudienciaId}")
	public ResponseEntity<PautaDto> update(@PathVariable Long pautaDeAudienciaId,
			@RequestBody PautaDto pautaDto) {
		pautaDto = pautaService.editar(pautaDeAudienciaId, pautaDto);
		if (pautaDto == null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		return ResponseEntity.ok(pautaDto);
	}

	@DeleteMapping("/{pautaDeAudienciaId}")
	public ResponseEntity<Void> delete(@PathVariable Long pautaDeAudienciaId) {
		pautaService.excluir(pautaDeAudienciaId);
		return ResponseEntity.noContent().build();
	}

	/*------------------------------------------------
	MANIPULADOR DE EXCESSÕES
	------------------------------------------------*/

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleValidationExceptio(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();

			errors.put(fieldName, errorMessage);
		});
		return errors;
	}

	/*------------------------------------------------
	ACTIONS DE DESENVOLVIMENTO
	------------------------------------------------*/

	@PutMapping("/truncate")
	@Transactional
	public ResponseEntity<Void> truncatePautistaTable() {
		pautaRepository.truncateTable();
		return ResponseEntity.noContent().build();
	}

}
