package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.AgeDependentHealthList;
import uk.co.ramp.io.types.ImmutableAgeDependentHealth;
import uk.co.ramp.io.types.ImmutableAgeDependentHealthList;
import uk.co.ramp.utilities.ImmutableMinMax;

public class AgeDependentHealthReader {

  public AgeDependentHealthList read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.create().fromJson(reader, AgeDependentHealthList.class);
  }

  public void create(Writer writer) {

    AgeDependentHealthList ageDependentHealthList =
        ImmutableAgeDependentHealthList.builder()
            .addAgeDependentList(
                ImmutableAgeDependentHealth.builder()
                    .range(ImmutableMinMax.of(0, 19))
                    .modifier(1.d)
                    .build())
            .addAgeDependentList(
                ImmutableAgeDependentHealth.builder()
                    .range(ImmutableMinMax.of(20, 39))
                    .modifier(0.9)
                    .build())
            .addAgeDependentList(
                ImmutableAgeDependentHealth.builder()
                    .range(ImmutableMinMax.of(40, 59))
                    .modifier(0.8)
                    .build())
            .addAgeDependentList(
                ImmutableAgeDependentHealth.builder()
                    .range(ImmutableMinMax.of(60, 79))
                    .modifier(0.6)
                    .build())
            .addAgeDependentList(
                ImmutableAgeDependentHealth.builder()
                    .range(ImmutableMinMax.of(80, 100))
                    .modifier(0.4)
                    .build())
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(ageDependentHealthList, writer);
  }
}
