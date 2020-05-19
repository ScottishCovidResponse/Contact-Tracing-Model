package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Optional;

@SpringBootConfiguration
public class AppConfig {

    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);

    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            LOGGER.trace("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                LOGGER.trace(beanName);
            }
        };
    }


    @Bean
    public StandardProperties standardProperties() throws IOException {
        try (Reader reader = new FileReader(new File("input/runSettings.json"))) {
            return new StandardPropertiesReader().read(reader);
        }
    }

    @Bean
    public DiseaseProperties diseaseProperties() throws IOException {
        try (Reader reader = new FileReader(new File("input/diseaseSettings.json"))) {
            return new DiseasePropertiesReader().read(reader);
        }
    }

    @Bean
    public PopulationProperties populationProperties() throws IOException {
        try (Reader reader = new FileReader(new File("input/populationSettings.json"))) {
            return new PopulationPropertiesReader().read(reader);
        }
    }

    @Bean
    public RandomDataGenerator randomDataGenerator(@Value("${cmdLineArgument:#{null}}") Optional<String[]> argumentValue) throws IOException {
        long arg = 0;
        if (argumentValue.isPresent() && argumentValue.get().length > 0) {
            arg = Integer.parseInt(argumentValue.get()[0]);
            LOGGER.info("Additional Seed information provided, the seed will be {}", standardProperties().seed() + arg);
        } else {
            LOGGER.info("Additional Seed information not provided, defaulting to {}", standardProperties().seed());
        }
        RandomDataGenerator r = new RandomDataGenerator();
        r.reSeed(standardProperties().seed() + arg);
        return r;
    }


}
