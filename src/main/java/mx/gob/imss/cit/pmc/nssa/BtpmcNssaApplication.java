package mx.gob.imss.cit.pmc.nssa;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BtpmcNssaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BtpmcNssaApplication.class, args);
	}

}

