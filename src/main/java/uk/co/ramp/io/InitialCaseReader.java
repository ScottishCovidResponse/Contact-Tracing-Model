package uk.co.ramp.io;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.csv.CsvReader;
import uk.co.ramp.io.types.ImmutableInitialCase;
import uk.co.ramp.io.types.InputFiles;
import uk.co.ramp.io.types.StandardProperties;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InitialCaseReader {

    private static final Logger LOGGER = LogManager.getLogger(InitialCaseReader.class);
    private final RandomDataGenerator generator;
    private final StandardProperties standardProperties;
    private Set<Integer> cases = new HashSet<>();

    @Autowired
    public InitialCaseReader(RandomDataGenerator generator, StandardProperties standardProperties, InputFiles inputFiles) {
        this.generator = generator;
        this.standardProperties = standardProperties;
        tryPopulateCases(inputFiles);
    }

    void tryPopulateCases(InputFiles inputFiles) {
        try (Reader reader = new FileReader(inputFiles.initialExposures())) {
            read(reader);
        } catch (Exception e) {
            LOGGER.warn("An IOException was thrown while populating the initial cases.");
            LOGGER.warn(e.getMessage());
        } finally {
            LOGGER.warn("The set of initial cases is {} long, when {} was requested", cases.size(), standardProperties.initialExposures());
            fillOrTrimSet();
            assert (cases.size() == standardProperties.initialExposures());
            LOGGER.info("The set is {} long and complies with the input", cases.size());
        }
    }

    void read(Reader reader) throws IOException {

        int population = standardProperties.populationSize();

        List<ImmutableInitialCase> list = new CsvReader().read(reader, ImmutableInitialCase.class);

        LOGGER.info("Read in a list of {} entries", list.size());

        // remove duplicates
        cases = list.stream().map(ImmutableInitialCase::id).filter(integer -> integer < population).collect(Collectors.toSet());

        LOGGER.info("After removing duplicates and limiting to values below the population size, {}, there are {} entries", population, cases.size());

    }

    void fillOrTrimSet() {
        int population = standardProperties.populationSize();
        int personLimit = standardProperties.initialExposures();

        Set<Integer> set = new HashSet<>(cases);

        if (set.size() > personLimit) {
            LOGGER.warn("The list of initial infections contains {} cases, but the limit has been set to {}", set.size(), personLimit);
            LOGGER.warn("The list will be trimmed to a size of {}", set.size());
            set = set.stream().limit(personLimit).collect(Collectors.toSet());
        } else if (set.size() < personLimit) {
            LOGGER.warn("The list initial infections contains {} entries, but the input has specified {}.", set.size(), personLimit);
            LOGGER.warn("The additional {} infections will be randomly assigned.", personLimit - set.size());
            while (set.size() < personLimit) {
                set.add(generator.nextInt(0, population - 1));
            }
        } else {
            LOGGER.info("The correct number of entries have been read in.");
        }

        cases = set;
    }


    public Set<Integer> getCases() {
        return cases;
    }
}
