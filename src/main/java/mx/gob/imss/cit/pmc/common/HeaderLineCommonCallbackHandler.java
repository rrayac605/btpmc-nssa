package mx.gob.imss.cit.pmc.common;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mx.gob.imss.cit.pmc.commons.callbackhandler.HeaderLineCallbackHandler;
import mx.gob.imss.cit.pmc.commons.dto.ArchivoDTO;
import mx.gob.imss.cit.pmc.commons.dto.CabeceraDTO;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;
import mx.gob.imss.cit.pmc.commons.utils.Utils;
import mx.gob.imss.cit.pmc.services.dao.archivo.ArchivoRepository;

@Component
@StepScope
public abstract class HeaderLineCommonCallbackHandler extends HeaderLineCallbackHandler
		implements LineCallbackHandler, StepExecutionListener {

	protected StepExecution stepExecution;

	@Autowired
	protected ArchivoRepository archivoRepository;

	public StepExecution getStepExecution() {
		return stepExecution;
	}

	public void setStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	@Override
	public void handleLine(String line) {
		stepExecution.getExecutionContext().put("Cabecera", crearCabecera(line));
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	public ExitStatus afterStep(StepExecution stepExecution, IdentificadorArchivoEnum identificador) {

		String nombre = stepExecution.getJobParameters().getString("nombre");

		if (!archivoRepository.existeArchivo(nombre != null ? nombre : Utils.obtenerNombreArchivoNSSA(identificador.getIdentificador()))) {

			CabeceraDTO cabeceraDTO = (CabeceraDTO) stepExecution.getExecutionContext().get("Cabecera");
			ArchivoDTO archivoDTO = null;
			if (cabeceraDTO.getNumRegistros().intValue() != stepExecution.getReadCount()) {
				archivoDTO = crearArchivoNSSA(identificador.getIdentificador(), stepExecution.getReadCount());
				stepExecution.setExitStatus(ExitStatus.FAILED);
			} else {
				archivoDTO = crearArchivoCorrectoNSSA(identificador.getIdentificador(), stepExecution.getReadCount());
			}
			if ((nombre != null && !nombre.trim().equals(""))) {
				archivoDTO.setNomArchivo(nombre);
				archivoDTO.setDesIdArchivo(nombre);
			}
			archivoRepository.saveUser(archivoDTO);
		}
		return stepExecution.getExitStatus();
	}

}
