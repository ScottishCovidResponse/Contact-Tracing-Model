package uk.co.ramp;

import org.apache.commons.math3.random.RandomDataGenerator;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Random;

public class TestUtils {

    public static Random getRandom() {
        return new Random(123);
    }

    public static RandomDataGenerator dataGenerator() {
        RandomDataGenerator r = new RandomDataGenerator();
        r.reSeed(123);
        return r;
    }

    public static DiseaseProperties diseaseProperties() throws FileNotFoundException {

        String file = TestUtils.class.getResource("/diseaseSettings.json").getFile();
        Reader reader = new FileReader(file);
        return new DiseasePropertiesReader().read(reader);

    }


}
