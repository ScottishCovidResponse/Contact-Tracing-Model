package uk.co.ramp.io.readers;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.InputFiles;

public class InputFilesReader {

  public InputFiles read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, InputFiles.class);
  }
}
