package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import uk.co.ramp.io.ImmutableStandardProperties;
import uk.co.ramp.io.StandardProperties;

import java.io.Reader;
import java.io.Writer;
import java.util.ServiceLoader;

public class StandardPropertiesReader {

    public StandardProperties read(Reader reader) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.setPrettyPrinting().create().fromJson(reader, StandardProperties.class);
    }

    public void create(Writer writer) {

        StandardProperties properties = ImmutableStandardProperties.builder()
                .populationSize(10000)
                .timeLimit(100)
                .infected(1000)
                .seed(0)
                .steadyState(true)
                .contactsFile("input/contacts.csv")
                .build();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(properties, writer);
    }


}
