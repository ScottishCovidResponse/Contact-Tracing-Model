package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uk.co.ramp.io.StandardProperties;

import java.io.*;

public class StandardPropertiesReader {

    private StandardPropertiesReader() {
        //hidden constructor
    }

    public static StandardProperties read(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader fileReader = new FileReader(file)) {
            return gson.fromJson(fileReader, StandardProperties.class);
        }
    }

    public static void create() throws IOException {

        StandardProperties properties = new StandardProperties(10000, 100, 1000, 0.01, 0, true);
        Writer w = new FileWriter("runSettings.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        gson.toJson(properties, w);

        w.flush();
        w.close();

    }


}
