package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.utilities.RandomSingleton;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@SpringBootConfiguration

public class AppConfig {

    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);

//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//        };
//    }


    @Bean
    public StandardProperties standardProperties() throws IOException {
        return StandardPropertiesReader.read(new File("input/runSettings.json"));
    }

    @Bean
    public DiseaseProperties diseaseProperties() throws IOException {
        return DiseasePropertiesReader.read(new FileReader(new File("input/diseaseSettings.json")));
    }

    @Bean
    public PopulationProperties populationProperties() throws IOException {
        return PopulationPropertiesReader.read(new File("input/populationSettings.json"));
    }

    @Bean
    public RandomDataGenerator randomDataGenerator(@Value("${cmdLineArgument:#{null}}") Optional<String[]> argumentValue) throws IOException {
        int arg = 0;
        if (argumentValue.isPresent() && argumentValue.get().length > 0) {
            arg = Integer.parseInt(argumentValue.get()[0]);
            LOGGER.info("Additional Seed information provided, the seed will be {}", standardProperties().getSeed() + arg);
        } else {
            LOGGER.info("Additional Seed information not provided, defaulting to {}", standardProperties().getSeed());
        }
        return RandomSingleton.getInstance(standardProperties().getSeed() + arg);
    }


}
