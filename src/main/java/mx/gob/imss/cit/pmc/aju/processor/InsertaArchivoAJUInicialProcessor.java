package mx.gob.imss.cit.pmc.aju.processor;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import mx.gob.imss.cit.pmc.common.InsertaArchivoInicialCommonProcessor;
import mx.gob.imss.cit.pmc.commons.dto.DetalleRegistroDTO;
import mx.gob.imss.cit.pmc.commons.dto.RegistroDTO;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;

@Component
@StepScope
public class InsertaArchivoAJUInicialProcessor extends InsertaArchivoInicialCommonProcessor
		implements ItemProcessor<RegistroDTO, DetalleRegistroDTO> {

	@Override
	public DetalleRegistroDTO process(RegistroDTO item) throws Exception {
		return procesar(item, IdentificadorArchivoEnum.ARCHIVO_AJU);
	}

}