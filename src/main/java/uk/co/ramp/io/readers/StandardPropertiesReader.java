package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.ImmutableStandardProperties;
import uk.co.ramp.io.types.StandardProperties;

public class StandardPropertiesReader {

  public StandardProperties read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, StandardProperties.class);
  }

  public void create(Writer writer) {

    StandardProperties properties =
        ImmutableStandardProperties.builder()
            .populationSize(10000)
            .timeLimitDays(100)
            .initialExposures(1000)
            .timeStepsPerDay(4)
            .timeStepSpread(0.25, 0.25, 0.25, 0.25)
            .seed(0)
            .steadyState(true)
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(properties, writer);
  }
}
