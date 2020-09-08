package uk.co.ramp.io.readers;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.ServiceLoader;
import uk.co.ramp.io.InfectionRates;
import uk.co.ramp.people.VirusStatus;

public class InfectionRateReader {

  public InfectionRates read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    Type empMapType = new TypeToken<Map<VirusStatus, Double>>() {}.getType();
    Map<VirusStatus, Double> type =
        gsonBuilder.setPrettyPrinting().create().fromJson(reader, empMapType);
    return new InfectionRates(type);
  }
}
