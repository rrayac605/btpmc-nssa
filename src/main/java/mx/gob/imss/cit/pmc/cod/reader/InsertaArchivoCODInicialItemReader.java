package mx.gob.imss.cit.pmc.cod.reader;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import mx.gob.imss.cit.pmc.cod.utils.HeaderLineCallbackHandlerCOD;
import mx.gob.imss.cit.pmc.commons.dto.RegistroDTO;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;
import mx.gob.imss.cit.pmc.commons.enums.ParametrosEnum;
import mx.gob.imss.cit.pmc.commons.exception.ParametroNoExisteException;
import mx.gob.imss.cit.pmc.commons.utils.Utils;
import mx.gob.imss.cit.pmc.nssa.enums.ItemReaderCamposEnum;
import mx.gob.imss.cit.pmc.nssa.enums.ItemReaderDelimiterEnum;
import mx.gob.imss.cit.pmc.services.ParametrosService;

@Component
@StepScope
public class InsertaArchivoCODInicialItemReader extends FlatFileItemReader<RegistroDTO> {

	private final static Logger logger = LoggerFactory.getLogger(InsertaArchivoCODInicialItemReader.class);

	@Value("#{jobParameters}")
	private Map<String, JobParameter> jobParameters;

	@Value("#{stepExecution}")
	private StepExecution stepExecution;

	@Autowired
	private HeaderLineCallbackHandlerCOD headerLineCallbackHandlerCOD;

	@Autowired
	private ParametrosService parametrosService;

	@PostConstruct
	public void init() {
		String nombre = stepExecution.getJobParameters().getString("nombre");
		setLineMapper(lineMapperInsertaArchivoInicialCOD());
		try {
			setResource(new FileSystemResource(parametrosService
					.obtenerParametro(ParametrosEnum.RUTA_DESTINO.getIdentificador()).getDesParametro()
					.concat((nombre != null && !nombre.trim().equals("")) ? nombre
							: Utils.obtenerNombreArchivoNSSA(IdentificadorArchivoEnum.ARCHIVO_COD.getIdentificador()))));
		} catch (ParametroNoExisteException e) {
			logger.error(e.getMessage(), e);
		}
		setLinesToSkip(1);
		setSkippedLinesCallback(headerLineCallbackHandlerCOD);
		setEncoding("UTF-8");
	}

	@Bean
	@StepScope
	public LineMapper<RegistroDTO> lineMapperInsertaArchivoInicialCOD() {
		DefaultLineMapper<RegistroDTO> lineMapper = new DefaultLineMapper<RegistroDTO>();
		lineMapper.setLineTokenizer(productLineTokenizerArchivoInicialCOD());
		lineMapper.setFieldSetMapper(productFieldSetMapperArchivoInicialCOD());
		return lineMapper;
	}

	@Bean
	@StepScope
	public LineTokenizer productLineTokenizerArchivoInicialCOD() {
		FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
		tokenizer.setColumns(ItemReaderDelimiterEnum.RANGOS_NSSA_CARGA_INICIAL.getValores());
		tokenizer.setNames(ItemReaderCamposEnum.CAMPOS_NSSA_CARGA_INICIAL.getValores());
		return tokenizer;
	}

	@Bean
	@StepScope
	public FieldSetMapper<RegistroDTO> productFieldSetMapperArchivoInicialCOD() {

		BeanWrapperFieldSetMapper<RegistroDTO> fieldSetMapper = new BeanWrapperFieldSetMapper<RegistroDTO>();
		fieldSetMapper.setTargetType(RegistroDTO.class);
		return fieldSetMapper;
	}

}