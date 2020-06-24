package uk.co.ramp.io.readers;

import static uk.co.ramp.distribution.ProgressionDistribution.FLAT;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableDiseaseProperties;
import uk.co.ramp.utilities.ImmutableMeanMax;
import uk.co.ramp.utilities.MeanMax;

public class DiseasePropertiesReader {

  public DiseaseProperties read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, DiseaseProperties.class);
  }

  public void create(Writer writer) {

    MeanMax meanMax = ImmutableMeanMax.builder().mean(5).max(8).build();

    DiseaseProperties wrapper =
        ImmutableDiseaseProperties.builder()
            .timeLatent(meanMax)
            .timeRecoveryAsymp(meanMax)
            .timeRecoverySymp(meanMax)
            .timeRecoverySev(meanMax)
            .timeSymptomsOnset(meanMax)
            .timeDecline(meanMax)
            .timeDeath(meanMax)
            .timeTestAdministered(meanMax)
            .timeTestResult(meanMax)
            .testAccuracy(0.95)
            .exposureThreshold(500)
            .exposureTuning(5)
            .progressionDistribution(FLAT)
            .randomInfectionRate(0.05)
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(wrapper, writer);
  }
}
