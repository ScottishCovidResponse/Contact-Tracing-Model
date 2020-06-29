package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;
import uk.co.ramp.io.types.ImmutableInputFiles;
import uk.co.ramp.io.types.InputFiles;

public class InputFilesReader {

  public InputFiles read(Reader reader) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
    return gsonBuilder.setPrettyPrinting().create().fromJson(reader, InputFiles.class);
  }

  public void create(Writer writer) {

    InputFiles properties =
        ImmutableInputFiles.builder()
            .runSettings("input/runSettings.json")
            .diseaseSettings("input/diseaseSettings.json")
            .populationSettings("input/populationSettings.json")
            .contactData("input/contactData.csv")
            .initialExposures("input/initialExposures.csv")
            .alertPolicies("input/alertPolicies.json")
            .isolationPolicies("input/isolationPolicies.json")
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(properties, writer);
  }
}
