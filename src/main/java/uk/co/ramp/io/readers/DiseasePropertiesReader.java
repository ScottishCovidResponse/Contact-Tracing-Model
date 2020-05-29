package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableDiseaseProperties;

import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;

import static uk.co.ramp.io.ProgressionDistribution.FLAT;

public class DiseasePropertiesReader {

    public DiseaseProperties read(Reader reader) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.setPrettyPrinting().create().fromJson(reader, DiseaseProperties.class);
    }

    public void create(Writer writer) {
        DiseaseProperties wrapper = ImmutableDiseaseProperties.builder()
                .meanTimeToInfectious(3)
                .meanTimeToInfected(3)
                .meanTimeToFinalState(7)
                .maxTimeToInfectious(14)
                .maxTimeToInfected(14)
                .maxTimeToFinalState(14)
                .randomInfectionRate(0.01)
                .meanTestTime(1)
                .maxTestTime(3)
                .testAccuracy(0.95)
                .exposureThreshold(10)
                .exposureTuning(160)
                .progressionDistribution(FLAT)
                .build();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(wrapper, writer);
    }


}
