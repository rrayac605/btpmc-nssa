package mx.gob.imss.cit.pmc.nssa.config;

import java.util.Objects;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.SneakyThrows;
import mx.gob.imss.cit.pmc.aju.processor.InsertaArchivoAJUInicialProcessor;
import mx.gob.imss.cit.pmc.aju.reader.ArchivoAJUInicialItemReader;
import mx.gob.imss.cit.pmc.aju.reader.InsertaArchivoAJUInicialItemReader;
import mx.gob.imss.cit.pmc.aju.tasklet.BuscarArchivoAJUTasklet;
import mx.gob.imss.cit.pmc.aju.tasklet.DuplicadosAJUTasklet;
import mx.gob.imss.cit.pmc.aju.tasklet.ObtenerArchivoAJUTasklet;
import mx.gob.imss.cit.pmc.aju.tasklet.SusceptiblesAJUTasklet;
import mx.gob.imss.cit.pmc.aju.utils.HeaderLineCallbackHandlerAJU;
import mx.gob.imss.cit.pmc.cod.processor.InsertaArchivoCODInicialProcessor;
import mx.gob.imss.cit.pmc.cod.reader.ArchivoCODInicialItemReader;
import mx.gob.imss.cit.pmc.cod.reader.InsertaArchivoCODInicialItemReader;
import mx.gob.imss.cit.pmc.cod.tasklet.BuscarArchivoCODTasklet;
import mx.gob.imss.cit.pmc.cod.tasklet.DuplicadosCODTasklet;
import mx.gob.imss.cit.pmc.cod.tasklet.ObtenerArchivoCODTasklet;
import mx.gob.imss.cit.pmc.cod.tasklet.SusceptiblesCODTasklet;
import mx.gob.imss.cit.pmc.cod.utils.HeaderLineCallbackHandlerCOD;
import mx.gob.imss.cit.pmc.commons.dto.ArchivoDTO;
import mx.gob.imss.cit.pmc.commons.dto.DetalleRegistroDTO;
import mx.gob.imss.cit.pmc.commons.dto.RegistroDTO;
import mx.gob.imss.cit.pmc.nssa.processor.InsertaArchivoInicialProcessor;
import mx.gob.imss.cit.pmc.nssa.processor.InsertaArchivoProcessor;
import mx.gob.imss.cit.pmc.nssa.reader.ArchivoNSSAInicialItemReader;
import mx.gob.imss.cit.pmc.nssa.reader.ArchivoNSSAItemReader;
import mx.gob.imss.cit.pmc.nssa.reader.InsertaArchivoInicialItemReader;
import mx.gob.imss.cit.pmc.nssa.reader.InsertaArchivoItemReader;
import mx.gob.imss.cit.pmc.nssa.tasklet.BuscarArchivoTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.CifrasControlTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.DuplicadosTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.EmailArchivoNoExisteTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.EmailCargaExistenteTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.EmailRegistrosIncorrectosTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.ObtenerArchivoTasklet;
import mx.gob.imss.cit.pmc.nssa.tasklet.SusceptiblesTasklet;
import mx.gob.imss.cit.pmc.nssa.utils.ArchivoNSSAReadListener;
import mx.gob.imss.cit.pmc.nssa.utils.HeaderLineCallbackHandlerNSSA;
import mx.gob.imss.cit.pmc.nssa.writer.ArchivoNSSAWriter;
import mx.gob.imss.cit.pmc.nssa.writer.InsertaArchivoWriter;
import mx.gob.imss.cit.pmc.rod.processor.InsertaArchivoRODInicialProcessor;
import mx.gob.imss.cit.pmc.rod.reader.ArchivoRODInicialItemReader;
import mx.gob.imss.cit.pmc.rod.reader.InsertaArchivoRODInicialItemReader;
import mx.gob.imss.cit.pmc.rod.tasklet.BuscarArchivoRODTasklet;
import mx.gob.imss.cit.pmc.rod.tasklet.DuplicadosRODTasklet;
import mx.gob.imss.cit.pmc.rod.tasklet.ObtenerArchivoRODTasklet;
import mx.gob.imss.cit.pmc.rod.tasklet.SusceptiblesRODTasklet;
import mx.gob.imss.cit.pmc.rod.utils.HeaderLineCallbackHandlerROD;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = { "mx.gob.imss.cit.pmc", "mailTemplates", "static.images" })
public class BatchConfiguration extends DefaultBatchConfigurer {

	// Common

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private EmailRegistrosIncorrectosTasklet emailRegistrosIncorrectosTasklet;

	@Autowired
	private EmailCargaExistenteTasklet emailCargaExistenteTasklet;

	@Autowired
	private EmailArchivoNoExisteTasklet emailArchivoNoExisteTasklet;

	@Autowired
	private ArchivoNSSAReadListener archivoNSSAReadListener;

	@Autowired
	private InsertaArchivoWriter insertaArchivoWriter;

	@Autowired
	private DuplicadosTasklet duplicadosTasklet;

	@Autowired
	private SusceptiblesTasklet susceptiblesTasklet;

	@Autowired
	private CifrasControlTasklet cifrasControlTasklet;

	// Nssa

	@Autowired
	private HeaderLineCallbackHandlerNSSA headerLineCallbackHandler;

	@Autowired
	private ArchivoNSSAItemReader archivoNSSAItemReader;

	@Autowired
	private ArchivoNSSAInicialItemReader archivoNSSAInicialItemReader;

	@Autowired
	private ArchivoNSSAWriter archivoNSSAWriter;

	@Autowired
	private InsertaArchivoItemReader insertaArchivoItemReader;

	@Autowired
	private InsertaArchivoProcessor insertaArchivoProcessor;

	@Autowired
	private ObtenerArchivoTasklet obtenerArchivoTasklet;

	@Autowired
	private BuscarArchivoTasklet buscarArchivoTasklet;

	// Nssa inicial

	@Autowired
	private InsertaArchivoInicialItemReader insertaArchivoInicialItemReader;

	@Autowired
	private InsertaArchivoInicialProcessor insertaArchivoInicialProcessor;

	// AJU

	@Autowired
	private ArchivoAJUInicialItemReader archivoAJUInicialItemReader;

	@Autowired
	private InsertaArchivoAJUInicialItemReader insertaArchivoAJUInicialItemReader;

	@Autowired
	private InsertaArchivoAJUInicialProcessor insertaArchivoAJUInicialProcessor;

	@Autowired
	private BuscarArchivoAJUTasklet buscarArchivoTaskletAJU;

	@Autowired
	private ObtenerArchivoAJUTasklet obtenerArchivoAJUTasklet;

	@Autowired
	private HeaderLineCallbackHandlerAJU headerLineCallbackHandlerAJU;

	@Autowired
	private DuplicadosAJUTasklet duplicadosAJUTasklet;

	@Autowired
	private SusceptiblesAJUTasklet susceptiblesAJUTasklet;

	// COD

	@Autowired
	private ArchivoCODInicialItemReader archivoCODInicialItemReader;

	@Autowired
	private InsertaArchivoCODInicialItemReader insertaArchivoCODInicialItemReader;

	@Autowired
	private InsertaArchivoCODInicialProcessor insertaArchivoCODInicialProcessor;

	@Autowired
	private BuscarArchivoCODTasklet buscarArchivoTaskletCOD;

	@Autowired
	private ObtenerArchivoCODTasklet obtenerArchivoCODTasklet;

	@Autowired
	private HeaderLineCallbackHandlerCOD headerLineCallbackHandlerCOD;

	@Autowired
	private DuplicadosCODTasklet duplicadosCODTasklet;

	@Autowired
	private SusceptiblesCODTasklet susceptiblesCODTasklet;

	// ROD

	@Autowired
	private ArchivoRODInicialItemReader archivoRODInicialItemReader;

	@Autowired
	private InsertaArchivoRODInicialItemReader insertaArchivoRODInicialItemReader;

	@Autowired
	private InsertaArchivoRODInicialProcessor insertaArchivoRODInicialProcessor;

	@Autowired
	private BuscarArchivoRODTasklet buscarArchivoTaskletROD;

	@Autowired
	private ObtenerArchivoRODTasklet obtenerArchivoRODTasklet;

	@Autowired
	private HeaderLineCallbackHandlerROD headerLineCallbackHandlerROD;

	@Autowired
	private DuplicadosRODTasklet duplicadosRODTasklet;

	@Autowired
	private SusceptiblesRODTasklet susceptiblesRODTasklet;

	@Bean
	@NonNull
	public PlatformTransactionManager getTransactionManager() {
		return new ResourcelessTransactionManager();
	}

	@SneakyThrows
	@NonNull
	@Bean
	public JobRepository getJobRepository() {
		return Objects.requireNonNull(new MapJobRepositoryFactoryBean(getTransactionManager()).getObject());
	}

	@Bean
	public Job readNSSA() {
		return jobBuilderFactory.get("readNSSA").incrementer(new RunIdIncrementer()).start(stepObtieneArchivo())
		.on("FAILED")
		.to(stepEnviaCorreoErrorNoExisteArchivo()).from(stepObtieneArchivo()).on("*")
		.to(validaExistenciaArchivo()).on("FAILED")
		.to(stepEnviaCorreoErrorCargaExiste()).from(validaExistenciaArchivo()).on("*")
		.to(stepValidaEncabezado()).on("FAILED")
		.to(stepEnviaCorreoErrorRegistrosIncorrectos()).from(stepValidaEncabezado()).on("*")
		.to(stepInsertaRegistroArchivo()).on("*")
		.to(stepDuplicados()).on("*")
		.to(stepSusceptibles()).on("*")
		.to(stepCifrasControl())
		.end()
		.build();			
	}

	@Bean
	public Job readNSSAInicial() {
		return jobBuilderFactory.get("readNSSAInicial").incrementer(new RunIdIncrementer()).start(stepObtieneArchivo())
				.on("FAILED").to(stepEnviaCorreoErrorNoExisteArchivo()).from(stepObtieneArchivo()).on("*")
				.to(validaExistenciaArchivo()).on("FAILED").to(stepEnviaCorreoErrorCargaExiste())
				.from(validaExistenciaArchivo()).on("*").to(stepValidaEncabezadoInicial()).on("FAILED")
				.to(stepEnviaCorreoErrorRegistrosIncorrectos()).from(stepValidaEncabezadoInicial()).on("*")
				.to(stepInsertaRegistroInicialArchivo()).on("*").to(stepDuplicados()).on("*").to(stepSusceptibles())
				.on("*").to(stepCifrasControl()).end().build();
	}

	@Bean
	public Job readAJUInicial() {
		return jobBuilderFactory.get("readAJUInicial").incrementer(new RunIdIncrementer())
				.start(stepObtieneArchivoAJU()).on("FAILED").to(stepEnviaCorreoErrorNoExisteArchivo())
				.from(stepObtieneArchivoAJU()).on("*").to(validaExistenciaArchivoAJU()).on("FAILED")
				.to(stepEnviaCorreoErrorCargaExiste()).from(validaExistenciaArchivoAJU()).on("*")
				.to(stepValidaEncabezadoInicialAJU()).on("FAILED").to(stepEnviaCorreoErrorRegistrosIncorrectos())
				.from(stepValidaEncabezadoInicialAJU()).on("*").to(stepInsertaRegistroInicialArchivo()).on("*")
				.to(stepDuplicadosAJU()).on("*").to(stepSusceptiblesAJU()).on("*").to(stepCifrasControlAJU()).end()
				.build();
	}

	@Bean
	public Job readCODInicial() {
		return jobBuilderFactory.get("readCODInicial").incrementer(new RunIdIncrementer())
				.start(stepObtieneArchivoCOD()).on("FAILED").to(stepEnviaCorreoErrorNoExisteArchivo())
				.from(stepObtieneArchivoCOD()).on("*").to(validaExistenciaArchivoCOD()).on("FAILED")
				.to(stepEnviaCorreoErrorCargaExiste()).from(validaExistenciaArchivoCOD()).on("*")
				.to(stepValidaEncabezadoInicialCOD()).on("FAILED").to(stepEnviaCorreoErrorRegistrosIncorrectos())
				.from(stepValidaEncabezadoInicialCOD()).on("*").to(stepInsertaRegistroInicialArchivoCOD()).on("*")
				.to(stepDuplicadosCOD()).on("*").to(stepSusceptiblesCOD()).on("*").to(stepCifrasControlROD()).end()
				.build();
	}

	@Bean
	public Job readRODInicial() {
		return jobBuilderFactory.get("readRODInicial").incrementer(new RunIdIncrementer())
				.start(stepObtieneArchivoROD()).on("FAILED").to(stepEnviaCorreoErrorNoExisteArchivo())
				.from(stepObtieneArchivoROD()).on("*").to(validaExistenciaArchivoROD()).on("FAILED")
				.to(stepEnviaCorreoErrorCargaExiste()).from(validaExistenciaArchivoROD()).on("*")
				.to(stepValidaEncabezadoInicialROD()).on("FAILED").to(stepEnviaCorreoErrorRegistrosIncorrectos())
				.from(stepValidaEncabezadoInicialROD()).on("*").to(stepInsertaRegistroInicialArchivoROD()).on("*")
				.to(stepDuplicadosROD()).on("*").to(stepSusceptiblesROD()).on("*").to(stepCifrasControlROD()).end()
				.build();
	}

	@Bean
	public Step stepObtieneArchivo() {
		return stepBuilderFactory.get("stepObtieneArchivo").tasklet(obtenerArchivoTasklet).build();
	}

	@Bean
	public Step stepObtieneArchivoAJU() {
		return stepBuilderFactory.get("stepObtieneArchivo").tasklet(obtenerArchivoAJUTasklet).build();
	}

	@Bean
	public Step stepObtieneArchivoCOD() {
		return stepBuilderFactory.get("stepObtieneArchivo").tasklet(obtenerArchivoCODTasklet).build();
	}

	@Bean
	public Step stepObtieneArchivoROD() {
		return stepBuilderFactory.get("stepObtieneArchivo").tasklet(obtenerArchivoRODTasklet).build();
	}

	@Bean
	public Step stepEnviaCorreoErrorRegistrosIncorrectos() {
		return stepBuilderFactory.get("stepEnviaCorreoErrorRegistrosIncorrectos")
				.tasklet(emailRegistrosIncorrectosTasklet).build();
	}

	@Bean
	public Step stepEnviaCorreoErrorCargaExiste() {
		return stepBuilderFactory.get("stepEnviaCorreoErrorCargaExiste").tasklet(emailCargaExistenteTasklet).build();
	}

	@Bean
	public Step stepEnviaCorreoErrorNoExisteArchivo() {
		return stepBuilderFactory.get("stepEnviaCorreoErrorNoExisteArchivo").tasklet(emailArchivoNoExisteTasklet)
				.build();
	}

	@Bean
	public Step stepSusceptibles() {
		return stepBuilderFactory.get("stepSusceptibles").tasklet(susceptiblesTasklet).build();
	}

	@Bean
	public Step stepSusceptiblesAJU() {
		return stepBuilderFactory.get("stepSusceptiblesAJU").tasklet(susceptiblesAJUTasklet).build();
	}

	@Bean
	public Step stepSusceptiblesCOD() {
		return stepBuilderFactory.get("stepSusceptiblesCOD").tasklet(susceptiblesCODTasklet).build();
	}

	@Bean
	public Step stepSusceptiblesROD() {
		return stepBuilderFactory.get("stepSusceptiblesROD").tasklet(susceptiblesRODTasklet).build();
	}

	@Bean
	public Step stepDuplicados() {
		return stepBuilderFactory.get("stepDuplicados").tasklet(duplicadosTasklet).build();
	}

	@Bean
	public Step stepDuplicadosAJU() {
		return stepBuilderFactory.get("stepDuplicadosAJU").tasklet(duplicadosAJUTasklet).build();
	}

	@Bean
	public Step stepDuplicadosCOD() {
		return stepBuilderFactory.get("stepDuplicadosCOD").tasklet(duplicadosCODTasklet).build();
	}

	@Bean
	public Step stepDuplicadosROD() {
		return stepBuilderFactory.get("stepDuplicadosROD").tasklet(duplicadosRODTasklet).build();
	}

	@Bean
	public Step stepCifrasControl() {
		return stepBuilderFactory.get("stepCifrasControl").tasklet(cifrasControlTasklet).build();
	}

	@Bean
	public Step stepCifrasControlAJU() {
		return stepBuilderFactory.get("stepCifrasControlAJU").tasklet(cifrasControlTasklet).build();
	}

	@Bean
	public Step stepCifrasControlCOD() {
		return stepBuilderFactory.get("stepCifrasControlCOD").tasklet(cifrasControlTasklet).build();
	}

	@Bean
	public Step stepCifrasControlROD() {
		return stepBuilderFactory.get("stepCifrasControlROD").tasklet(cifrasControlTasklet).build();
	}

	@Bean
	public Step validaExistenciaArchivo() {
		return stepBuilderFactory.get("validaExistenciaArchivo").tasklet(buscarArchivoTasklet).build();
	}

	@Bean
	public Step validaExistenciaArchivoAJU() {
		return stepBuilderFactory.get("validaExistenciaArchivo").tasklet(buscarArchivoTaskletAJU).build();
	}

	@Bean
	public Step validaExistenciaArchivoCOD() {
		return stepBuilderFactory.get("validaExistenciaArchivo").tasklet(buscarArchivoTaskletCOD).build();
	}

	@Bean
	public Step validaExistenciaArchivoROD() {
		return stepBuilderFactory.get("validaExistenciaArchivo").tasklet(buscarArchivoTaskletROD).build();
	}

	@Bean
	public Step stepValidaEncabezado() {
		return stepBuilderFactory.get("stepValidaEncabezado").listener(headerLineCallbackHandler)
				.<RegistroDTO, ArchivoDTO>chunk(1000000).reader(archivoNSSAItemReader).listener(archivoNSSAReadListener)
				.writer(archivoNSSAWriter).build();
	}

	@Bean
	public Step stepValidaEncabezadoInicial() {
		return stepBuilderFactory.get("stepValidaEncabezado").listener(headerLineCallbackHandler)
				.<RegistroDTO, ArchivoDTO>chunk(1000000).reader(archivoNSSAInicialItemReader)
				.listener(archivoNSSAReadListener).writer(archivoNSSAWriter).build();
	}

	@Bean
	public Step stepValidaEncabezadoInicialAJU() {
		return stepBuilderFactory.get("stepValidaEncabezadoInicialAJU").listener(headerLineCallbackHandlerAJU)
				.<RegistroDTO, ArchivoDTO>chunk(1000000).reader(archivoAJUInicialItemReader)
				.listener(archivoNSSAReadListener).writer(archivoNSSAWriter).build();
	}

	@Bean
	public Step stepValidaEncabezadoInicialCOD() {
		return stepBuilderFactory.get("stepValidaEncabezadoInicialCOD").listener(headerLineCallbackHandlerCOD)
				.<RegistroDTO, ArchivoDTO>chunk(1000000).reader(archivoCODInicialItemReader)
				.listener(archivoNSSAReadListener).writer(archivoNSSAWriter).build();
	}

	@Bean
	public Step stepValidaEncabezadoInicialROD() {
		return stepBuilderFactory.get("stepValidaEncabezadoInicialROD").listener(headerLineCallbackHandlerROD)
				.<RegistroDTO, ArchivoDTO>chunk(1000000).reader(archivoRODInicialItemReader)
				.listener(archivoNSSAReadListener).writer(archivoNSSAWriter).build();
	}

	@Bean
	public Step stepInsertaRegistroArchivo() {
		return stepBuilderFactory.get("stepInsertaRegistroArchivo").listener(headerLineCallbackHandler)
				.<RegistroDTO, DetalleRegistroDTO>chunk(100).reader(insertaArchivoItemReader)
				.processor(insertaArchivoProcessor).writer(insertaArchivoWriter).build();
	}

	@Bean
	public Step stepInsertaRegistroInicialArchivo() {
		return stepBuilderFactory.get("stepInsertaRegistroInicialArchivo").listener(headerLineCallbackHandler)
				.<RegistroDTO, DetalleRegistroDTO>chunk(100).reader(insertaArchivoInicialItemReader)
				.processor(insertaArchivoInicialProcessor).writer(insertaArchivoWriter).build();
	}

	@Bean
	public Step stepInsertaRegistroInicialArchivoAJU() {
		return stepBuilderFactory.get("stepInsertaRegistroInicialArchivoAJU").listener(headerLineCallbackHandlerAJU)
				.<RegistroDTO, DetalleRegistroDTO>chunk(100).reader(insertaArchivoAJUInicialItemReader)
				.processor(insertaArchivoAJUInicialProcessor).writer(insertaArchivoWriter).build();
	}

	@Bean
	public Step stepInsertaRegistroInicialArchivoCOD() {
		return stepBuilderFactory.get("stepInsertaRegistroInicialArchivoCOD").listener(headerLineCallbackHandlerCOD)
				.<RegistroDTO, DetalleRegistroDTO>chunk(100).reader(insertaArchivoCODInicialItemReader)
				.processor(insertaArchivoCODInicialProcessor).writer(insertaArchivoWriter).build();
	}

	@Bean
	public Step stepInsertaRegistroInicialArchivoROD() {
		return stepBuilderFactory.get("stepInsertaRegistroInicialArchivoROD").listener(headerLineCallbackHandlerROD)
				.<RegistroDTO, DetalleRegistroDTO>chunk(100).reader(insertaArchivoRODInicialItemReader)
				.processor(insertaArchivoRODInicialProcessor).writer(insertaArchivoWriter).build();
	}

}