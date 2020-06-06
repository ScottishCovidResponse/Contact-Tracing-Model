package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.utilities.UtilitiesBean;

import java.io.*;
import java.util.Optional;

@SpringBootConfiguration
@ComponentScan
public class AppConfig {

    private static final String RUN_SETTINGS_LOCATION = "input/runSettings.json";
    private static final String DISEASE_SETTINGS_LOCATION = "input/diseaseSettings.json";
    private static final String POPULATION_SETTINGS_LOCATION = "input/populationSettings.json";


    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);


    @Bean
    public StandardProperties standardProperties() throws ConfigurationException {
        try (Reader reader = getReader(RUN_SETTINGS_LOCATION)) {
            return new StandardPropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the run properties at " + RUN_SETTINGS_LOCATION;
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }

    @Bean
    public DiseaseProperties diseaseProperties() throws ConfigurationException {
        try (Reader reader = getReader(DISEASE_SETTINGS_LOCATION)) {
            return new DiseasePropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the disease properties at " + DISEASE_SETTINGS_LOCATION;
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }

    @Bean
    public PopulationProperties populationProperties() throws ConfigurationException {
        try (Reader reader = getReader(POPULATION_SETTINGS_LOCATION)) {
            return new PopulationPropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the population properties at " + POPULATION_SETTINGS_LOCATION;
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }

    Reader getReader(String input) throws FileNotFoundException {
        return new FileReader(new File(input));
    }


    @Bean
    public RandomDataGenerator randomDataGenerator(@Value("${cmdLineArgument:#{null}}") Optional<String[]> argumentValue) throws ConfigurationException {
        long arg = 0;
        try {
            if (argumentValue.isPresent() && argumentValue.get().length > 0) {
                arg = Integer.parseInt(argumentValue.get()[0]);
                LOGGER.info("Additional Seed information provided, the seed will be {}", standardProperties().seed() + arg);
            } else {
                LOGGER.info("Additional Seed information not provided, defaulting to {}", standardProperties().seed());
            }
            RandomDataGenerator r = new RandomDataGenerator();
            r.reSeed(standardProperties().seed() + arg);
            return r;
        } catch (NullPointerException | ConfigurationException e) {
            String message = "An error occurred while creating the random generator. This is likely due to an error in Standard Properties";
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        } catch (NumberFormatException e) {
            String message = argumentValue
                    .map(strings -> "An error occurred while creating the random generator. The command line arg, \"" + strings[0] + "\", could not be parsed as an integer.").
                            orElse("An unknown NumberFormatException occurred while creating the random number generator.");
            LOGGER.error(message);
            throw new ConfigurationException(message, e);

        }

    }

    @Bean
    public UtilitiesBean utilitiesBean() {
        return new UtilitiesBean();
    }


}
