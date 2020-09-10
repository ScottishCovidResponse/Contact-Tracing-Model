package uk.co.ramp.io.readers;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.PopulationOverrides;

public class PopulationOverridesReader {

  public PopulationOverrides read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.create().fromJson(reader, PopulationOverrides.class);
  }
}
