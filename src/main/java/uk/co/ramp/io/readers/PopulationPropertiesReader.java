package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.ImmutablePopulationProperties;
import uk.co.ramp.io.types.PopulationProperties;
import uk.co.ramp.utilities.ImmutableMinMax;

public class PopulationPropertiesReader {

  public PopulationProperties read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, PopulationProperties.class);
  }

  public void create(Writer writer) {

    // index and proportion
    // data taken from census
    PopulationProperties wrapper =
        ImmutablePopulationProperties.builder()
            .putPopulationDistribution(0, 0.1759)
            .putPopulationDistribution(1, 0.1171)
            .putPopulationDistribution(2, 0.4029)
            .putPopulationDistribution(3, 0.1222)
            .putPopulationDistribution(4, 0.1819)
            .putPopulationAges(0, ImmutableMinMax.of(0, 14))
            .putPopulationAges(1, ImmutableMinMax.of(15, 24))
            .putPopulationAges(2, ImmutableMinMax.of(25, 54))
            .putPopulationAges(3, ImmutableMinMax.of(55, 64))
            .putPopulationAges(4, ImmutableMinMax.of(65, 90))
            .genderBalance(0.99)
            .appUptake(0.7)
            .testCapacity(0.01)
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(wrapper, writer);
  }
}
