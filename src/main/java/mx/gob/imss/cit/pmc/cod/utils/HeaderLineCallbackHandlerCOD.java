package mx.gob.imss.cit.pmc.cod.utils;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import mx.gob.imss.cit.pmc.common.HeaderLineCommonCallbackHandler;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;

@Component
@StepScope
public class HeaderLineCallbackHandlerCOD extends HeaderLineCommonCallbackHandler {

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

		return afterStep(stepExecution, IdentificadorArchivoEnum.ARCHIVO_COD);
	}

}
