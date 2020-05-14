package uk.co.ramp;

import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.PopulationProperties;
import uk.co.ramp.io.StandardProperties;
import uk.co.ramp.io.readers.DiseasePropertiesReader;
import uk.co.ramp.io.readers.PopulationPropertiesReader;
import uk.co.ramp.io.readers.StandardPropertiesReader;

import java.io.File;
import java.io.IOException;

public class Properties {

    private static StandardProperties standardProperties;
    private static DiseaseProperties diseaseProperties;
    private static PopulationProperties populationProperties;

    private Properties() {

    }

    public static StandardProperties getStandardProperties() throws IOException {
        if (standardProperties == null) {
            standardProperties = StandardPropertiesReader.read(new File("input/runSettings.json"));
        }
        return standardProperties;
    }

    public static DiseaseProperties getDiseaseProperties() throws IOException {
        if (diseaseProperties == null) {
            diseaseProperties = DiseasePropertiesReader.read(new File("input/diseaseSettings.json"));
        }
        return diseaseProperties;
    }

    public static PopulationProperties getPopulationProperties() throws IOException {
        if (populationProperties == null) {
            populationProperties = PopulationPropertiesReader.read(new File("input/populationSettings.json"));
        }
        return populationProperties;
    }


}
