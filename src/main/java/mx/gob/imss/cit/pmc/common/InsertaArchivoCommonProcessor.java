package mx.gob.imss.cit.pmc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import mx.gob.imss.cit.pmc.commons.dto.ArchivoDTO;
import mx.gob.imss.cit.pmc.commons.dto.AseguradoDTO;
import mx.gob.imss.cit.pmc.commons.dto.CabeceraDTO;
import mx.gob.imss.cit.pmc.commons.dto.CatalogoDTO;
import mx.gob.imss.cit.pmc.commons.dto.DetalleRegistroDTO;
import mx.gob.imss.cit.pmc.commons.dto.PatronDTO;
import mx.gob.imss.cit.pmc.commons.dto.RegistroDTO;
import mx.gob.imss.cit.pmc.commons.enums.IdentificadorArchivoEnum;
import mx.gob.imss.cit.pmc.commons.enums.PasoAlEnum;
import mx.gob.imss.cit.pmc.commons.exception.AseguradonNoEncontradoException;
import mx.gob.imss.cit.pmc.commons.exception.PatronNoEncontradoException;
import mx.gob.imss.cit.pmc.commons.processor.ArchivoProcessor;
import mx.gob.imss.cit.pmc.commons.utils.Utils;
import mx.gob.imss.cit.pmc.services.CatalogosService;
import mx.gob.imss.cit.pmc.services.DetalleRegistroService;
import mx.gob.imss.cit.pmc.services.asegurado.AseguradoService;
import mx.gob.imss.cit.pmc.services.catalogos.CatalogosBDTUService;
import mx.gob.imss.cit.pmc.services.catalogos.CatalogosLocales;
import mx.gob.imss.cit.pmc.services.dao.archivo.ArchivoRepository;
import mx.gob.imss.cit.pmc.services.patrones.PatronesService;
import mx.gob.imss.cit.pmc.validation.launcher.LauncherNSSA;

public class InsertaArchivoCommonProcessor extends ArchivoProcessor {

	protected final static Logger logger = LoggerFactory.getLogger(InsertaArchivoCommonProcessor.class);

	@Value("#{jobParameters}")
	protected Map<String, JobParameter> jobParameters;

	@Value("#{stepExecution}")
	protected StepExecution stepExecution;

	@Autowired
	protected LauncherNSSA launcherNSSA;

	@Autowired
	protected PatronesService patronesService;

	@Autowired
	protected AseguradoService aseguradoService;

	@Autowired
	protected CatalogosBDTUService catalogosBDTUService;

	@Autowired
	protected CatalogosService catalogosService;

	@Autowired
	protected DetalleRegistroService detalleRegistroService;

	@Autowired
	protected CatalogosLocales catalogosLocales;

	@Autowired
	protected ArchivoRepository archivoRepository;

	public DetalleRegistroDTO procesar(RegistroDTO item, IdentificadorArchivoEnum identificador) throws PatronNoEncontradoException {
		String nombre = stepExecution.getJobParameters().getString("nombre");
		Integer indice = (Integer) stepExecution.getExecutionContext().get("indice");
		CabeceraDTO cabeceraDTO = (CabeceraDTO) stepExecution.getExecutionContext().get("Cabecera");
		ArchivoDTO archivoDTO = (ArchivoDTO) stepExecution.getExecutionContext().get("archivoDTO");
		@SuppressWarnings("unchecked")
		List<DetalleRegistroDTO> detalle = (List<DetalleRegistroDTO>) stepExecution.getExecutionContext()
				.get("detalleRegistroDTO");
		if (archivoDTO == null) {
			Optional<ArchivoDTO> archivoOptional = archivoRepository.findOneByName(
					nombre != null ? nombre : Utils.obtenerNombreArchivoNSSA(identificador.getIdentificador()));
			archivoDTO = archivoOptional.get();
			detalle = new ArrayList<DetalleRegistroDTO>();
			indice = 0;
			stepExecution.getExecutionContext().put("indice", indice);
			stepExecution.getExecutionContext().put("archivoDTO", archivoOptional.get());
		}
		PatronDTO patronDTO = patronesService.obtenerPatronOracle(item.getRefRegistroPatronal());
		item.setCveDelegacionPatron(patronDTO.getCveDelegacionAux());

		complementarDatosBDTU(item);
		complementarLocal(item, identificador);
		launcherNSSA.validaRegistro(item);
		DetalleRegistroDTO detalleRegistroDTO = new DetalleRegistroDTO();
		detalleRegistroDTO.setAseguradoDTO(procesarAseguradoDTO(item));
		detalleRegistroDTO.setIncapacidadDTO(procesarIncapacidadDTO(item));
		detalleRegistroDTO.setPatronDTO(procesarPatronDTO(patronDTO, item));
		if(detalleRegistroDTO.getAseguradoDTO().getSinUMF()) {
			asignaDelegacionPatronalAsegurado(detalleRegistroDTO.getAseguradoDTO(), detalleRegistroDTO.getPatronDTO());
		}
		try {
			validarMarcaAfiliatoria(aseguradoService.existeMarcaAfiliatoria(item.getNumNss()),
					detalleRegistroDTO.getAseguradoDTO());
		} catch (AseguradonNoEncontradoException e) {
			logger.error(e.getMessage(), e);
		}
		detalleRegistroDTO.setPatronDTO(procesarPatronDTO(patronDTO, item));
		detalleRegistroDTO.setBitacoraErroresDTO(item.getBitacoraErroresDTO());
		detalleRegistroDTO.setIdentificadorArchivo(new ObjectId(archivoDTO.getObjectIdArchivo()));
		detalleRegistroDTO.setCveOrigenArchivo(archivoDTO.getCveOrigenArchivo());
		detalleRegistroDTO.getAseguradoDTO().setNumIndice((Integer) stepExecution.getExecutionContext().get("indice"));
		detalleRegistroDTO.setAseguradoPasoAl(item.getAseguradoPasoAl());
		archivoDTO
				.setCifrasControlDTO(procesarCifrasControl(item.getCifrasControlDTO(), cabeceraDTO.getNumRegistros()));
		detalle.add(detalleRegistroDTO);
		indice++;
		stepExecution.getExecutionContext().put("indice", indice);
		stepExecution.getExecutionContext().put("archivoDTO", archivoDTO);
		stepExecution.getExecutionContext().put("detalleRegistroDTO", detalle);
		return detalleRegistroDTO;
	}

	private void complementarLocal(RegistroDTO item, IdentificadorArchivoEnum identificador) {
		item.setDesOcupacion(catalogosLocales.obtenerOcupacion(item.getCveOcupacion(), identificador).getDesCatalogo());
		CatalogoDTO consecuenciaDTO = catalogosLocales.obtenerConsecuencia(item.getCveConsecuencia());
		item.setDesConsecuencia(consecuenciaDTO != null ? consecuenciaDTO.getDesCatalogo() : null);

		item.setDesLaudo(catalogosLocales.obtenerLaudo(item.getNumLaudo()).getDesCatalogo());
		item.setDesNaturaleza(catalogosLocales.obtenerNaturaleza(item.getCveNaturaleza()).getDesCatalogo());
		item.setDesTipoRiesgo(catalogosLocales.obtenerTipoRiesgo(item.getCveTipoRiesgo()).getDesCatalogo());

		item.setDesTipoIncapacidad(
				catalogosLocales.obtenerTipoIncapacidad(item.getCveTipoIncapacidad()).getDesCatalogo());

		item.setDesCausaExterna(catalogosLocales.obtenerCausaExterna(item.getCveCausaExterna()).getDesCatalogo());
		item.setDesRiesgoFisico(catalogosLocales.obtenerRiesgoFisico(item.getCveRiesgoFisico()).getDesCatalogo());
		item.setDesCodigoDiagnostico(
				catalogosLocales.obtenerDiagnostico(item.getNumCodigoDiagnostico()).getDesCatalogo());

		item.setDesActoInseguro(catalogosLocales.obtenerActoInseguro(item.getCveActoInseguro()).getDesCatalogo());

	}

	private void complementarDatosBDTU(RegistroDTO item) {
		try {

			AseguradoDTO aseguradoDTO = null;
			if (item.getNomAsegurado() != null
					&& item.getNomAsegurado().contains(PasoAlEnum.PASO_AL.getDescripcion())) {
				String numNss = item.getNomAsegurado().substring(7, 18).trim();
				aseguradoDTO = aseguradoService.existeAseguradoOracle(numNss);
				procesaPasoAl(item, aseguradoDTO);
			} else {
				aseguradoDTO = aseguradoService.existeAseguradoOracle(item.getNumNss());
			}
			item.setNumNss(aseguradoDTO.getNumNss());

			item.setNssExisteBDTU(aseguradoDTO.getCveIdPersona() != null);

			item.setRefCurpBDTU(aseguradoDTO.getRefCurp());

			item.setRefNombreBDTU(aseguradoDTO.getNomAsegurado());
			item.setErrorNombre(
					item.getNomAsegurado() != null && !item.getNomAsegurado().trim().equals("") ? false : true);

			item.setErrorCurp(!(item.getRefCurp() == null || item.getRefCurp().trim().equals(""))
					? item.getRefCurp().matches("^[A-Za-z0-9]{18}$") == false
					: true);

			item.setDelegacionAdscripcionDTO(
					catalogosBDTUService.obtenerDelegacion(Utils.obtenerDelegacion(item, aseguradoDTO)));

			item.setSubDelegacionAdscripcionDTO(catalogosBDTUService.obtenerSubDelegacion(
					Utils.obtenerDelegacion(item, aseguradoDTO), Utils.obtenerSubDelegacion(item, aseguradoDTO)));

			item.setUmfAdscripcionDTO(catalogosBDTUService.obtenerUMF(Utils.obtenerDelegacion(item, aseguradoDTO),
					Utils.obtenerSubDelegacion(item, aseguradoDTO), Utils.obtenerUmf(item, aseguradoDTO)));

			item.setDelegacionAtencionDTO(catalogosBDTUService.obtenerDelegacion(item.getCveDelegacionAtencion()));

			item.setSubDelegacionAtencionDTO(catalogosBDTUService.obtenerSubDelegacion(item.getCveDelegacionAtencion(),
					item.getCveSubDelAtencion()));

			item.setUmfPagadoraDTO(catalogosBDTUService.obtenerUMF(item.getCveUmfPagadora()));

			item.setUmfExpedicionDTO(catalogosBDTUService.obtenerUMF(item.getCveUmfExp()));

			item.setTipoRiesgoDTO(catalogosService.obtenerTipoRiesgo(item.getCveTipoRiesgo()));

			item.setConsecuenciaDTO(catalogosService.obtenerConsecuencia(item.getCveConsecuencia()));

			item.setLaudoDTO(catalogosService.obtenerLaudo(item.getNumLaudo()));

			item.setTipoIncapacidadDTO(catalogosService.obtenerTipoIncapacidad(item.getCveTipoIncapacidad()));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

}