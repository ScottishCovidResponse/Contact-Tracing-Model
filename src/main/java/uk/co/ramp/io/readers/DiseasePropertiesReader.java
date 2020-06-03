package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableDiseaseProperties;
import uk.co.ramp.utilities.ImmutableMeanMax;
import uk.co.ramp.utilities.MeanMax;

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

        MeanMax meanMax = ImmutableMeanMax.builder().mean(5).max(8).build();

        DiseaseProperties wrapper = ImmutableDiseaseProperties.builder().
                timeLatent(meanMax).
                timeRecoveryAsymp(meanMax).
                timeRecoverySymp(meanMax).
                timeRecoverySev(meanMax).
                timeSymptomsOnset(meanMax).
                timeDecline(meanMax).
                timeDeath(meanMax).
                timeTestAdministered(meanMax).
                timeTestResult(meanMax).
                testAccuracy(0.95).
                exposureThreshold(10).
                exposureTuning(160).
                progressionDistribution(FLAT).
                build();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(wrapper, writer);
    }


}
