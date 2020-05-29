package uk.co.ramp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import uk.co.ramp.io.CompartmentWriter;
import uk.co.ramp.io.InitialCaseReader;
import uk.co.ramp.people.PopulationGenerator;
import uk.co.ramp.utilities.ContactReader;

import java.io.IOException;

@SpringBootApplication
public class Framework {

    public static void main(String[] args) throws IOException, ConfigurationException {
        SpringApplication.run(Framework.class, args);
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        registerServices(ctx);
        ContactRunner runner = (ContactRunner) ctx.getBean("contactRunner");
        runner.run();
    }

    private static void registerServices(AnnotationConfigApplicationContext ctx) {

        ctx.register(
                ContactRunner.class,
                Outbreak.class,
                PopulationGenerator.class,
                ContactReader.class,
                CompartmentWriter.class,
                InitialCaseReader.class);

    }


}
