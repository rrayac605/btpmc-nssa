package mx.gob.imss.cit.pmc.common;

import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import mx.gob.imss.cit.pmc.commons.dto.ArchivoDTO;
import mx.gob.imss.cit.pmc.commons.dto.CabeceraDTO;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;
import mx.gob.imss.cit.pmc.commons.processor.ArchivoProcessor;
import mx.gob.imss.cit.pmc.services.dao.archivo.ArchivoRepository;

public class InsertaArchivoCommonTasklet extends ArchivoProcessor {

	@Value("#{stepExecution}")
	private StepExecution stepExecution;

	@Autowired
	private ArchivoRepository archivoRepository;

	public void execute(IdentificadorArchivoEnum identificador) {
		CabeceraDTO cabeceraDTO = (CabeceraDTO) stepExecution.getExecutionContext().get("Cabecera");
		ArchivoDTO archivoDTO = creaArchivo(cabeceraDTO.getNumRegistros().toString(), identificador.getIdentificador());
		archivoDTO = archivoRepository.saveUser(archivoDTO);
		stepExecution.getExecutionContext().put("archivoDTO", archivoDTO);
	}

}
