package uk.co.ramp.policy.isolation;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.util.ServiceLoader;

class IsolationPropertiesReader {
  IsolationProperties read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, IsolationProperties.class);
  }
}
