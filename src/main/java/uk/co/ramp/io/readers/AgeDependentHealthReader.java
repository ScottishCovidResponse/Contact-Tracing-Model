package uk.co.ramp.io.readers;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.AgeDependentHealthList;

public class AgeDependentHealthReader {

  public AgeDependentHealthList read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.create().fromJson(reader, AgeDependentHealthList.class);
  }
}
