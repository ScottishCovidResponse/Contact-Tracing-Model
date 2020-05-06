package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.utilities.MinMax;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PopulationPropertiesReader {

    private PopulationPropertiesReader() {
        //hidden constructor
    }

    public static PopulationProperties read(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader fileReader = new FileReader(file)) {
            return gson.fromJson(fileReader, PopulationProperties.class);
        }
    }

    public static void create() throws IOException {

        // index and proportion
        Map<Integer, Double> populationDistribution = new HashMap<>();
        Map<Integer, MinMax> populationAges = new HashMap<>();

        // data taken from census
        populationDistribution.put(0, 0.1759);
        populationDistribution.put(1, 0.1171);
        populationDistribution.put(2, 0.4029);
        populationDistribution.put(3, 0.1222);
        populationDistribution.put(4, 0.1819);

        populationAges.put(0, new MinMax(0, 14));
        populationAges.put(1, new MinMax(15, 24));
        populationAges.put(2, new MinMax(25, 54));
        populationAges.put(3, new MinMax(55, 64));
        populationAges.put(4, new MinMax(65, 90));

        PopulationProperties wrapper = new PopulationProperties(populationDistribution, populationAges, 0.99);

        try (Writer w = new FileWriter("population.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(wrapper, w);
        }
    }

}
