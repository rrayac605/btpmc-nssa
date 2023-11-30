package mx.gob.imss.cit.pmc.nssa.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mx.gob.imss.cit.pmc.commons.mail.EmailTasklet;
import mx.gob.imss.cit.pmc.services.EmailParametrosService;
import mx.gob.imss.cit.pmc.services.EmailService;
import mx.gob.imss.cit.pmc.services.enums.CasoValidadoEnum;
import mx.gob.imss.cit.pmc.services.enums.PlantillaEmailEnum;
import mx.gob.imss.cit.pmc.services.enums.SistemaOrigenEnum;

@Component
@StepScope
public class EmailCargaExistenteTasklet extends EmailTasklet implements Tasklet {

	protected final static Logger logger = LoggerFactory.getLogger(EmailCargaExistenteTasklet.class);

	@Autowired
	private EmailService emailService;

	@Autowired
	private EmailParametrosService emailParametrosService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		return RepeatStatus.FINISHED;
	}
}
