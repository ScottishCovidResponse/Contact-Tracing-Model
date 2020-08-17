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
            .runSettings("runSettings.json")
            .diseaseSettings("diseaseSettings.json")
            .populationSettings("populationSettings.json")
            .contactData("contactData.csv")
            .ageData("ageData.csv")
            .initialExposures("initialExposures.csv")
            .tracingPolicies("tracingPolicies.json")
            .isolationPolicies("isolationPolicies.json")
            .infectionRates("infectionRates.json")
            .build();

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    gson.toJson(properties, writer);
  }
}
