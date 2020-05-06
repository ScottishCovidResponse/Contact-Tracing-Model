package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ProgressionDistribution;

import java.io.*;

public class DiseasePropertiesReader {

    private DiseasePropertiesReader() {
        //hidden constructor
    }

    public static DiseaseProperties read(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader fileReader = new FileReader(file)) {
            return gson.fromJson(fileReader, DiseaseProperties.class);
        }
    }

    public static void create() throws IOException {

        DiseaseProperties wrapper = new DiseaseProperties(3, 7, 0.01, ProgressionDistribution.FLAT);

        try (Writer w = new FileWriter("input/diseaseSettings.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(wrapper, w);
        }
    }


}
