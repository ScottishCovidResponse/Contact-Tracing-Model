package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.distribution.DistributionSampler;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.InputFiles;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.InputFilesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;
import uk.co.ramp.utilities.UtilitiesBean;

import java.io.*;
import java.util.Optional;

@SpringBootConfiguration
@ComponentScan
public class AppConfig {

    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
    private static final String INPUT_FILE = "input/inputLocations.json";

    @Bean
    public InputFiles inputFiles() throws ConfigurationException {
        try (Reader reader = getReader(INPUT_FILE)) {
            return new InputFilesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the run properties at " + INPUT_FILE;
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }

    }

    @Bean
    public StandardProperties standardProperties() throws ConfigurationException {
        try (Reader reader = getReader(inputFiles().runSettings())) {
            return new StandardPropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the run properties at " + inputFiles().runSettings();
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }

    @Bean
    public DiseaseProperties diseaseProperties() throws ConfigurationException {
        try (Reader reader = getReader(inputFiles().diseaseSettings())) {
            return new DiseasePropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the disease properties at " + inputFiles().diseaseSettings();
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }

    @Bean
    public PopulationProperties populationProperties() throws ConfigurationException {
        try (Reader reader = getReader(inputFiles().populationSettings())) {
            return new PopulationPropertiesReader().read(reader);
        } catch (IOException e) {
            String message = "An error occurred while parsing the population properties at " + inputFiles().populationSettings();
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
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
<<<<<<<<< Temporary merge branch 1
    public UtilitiesBean utilitiesBean() {
        return new UtilitiesBean();
    }

    @Bean
    public DistributionSampler distributionSampler(RandomDataGenerator randomDataGenerator) {
        return new DistributionSampler(randomDataGenerator);
    }

    Reader getReader(String input) throws FileNotFoundException {
        return new FileReader(new File(input));
    }

}
