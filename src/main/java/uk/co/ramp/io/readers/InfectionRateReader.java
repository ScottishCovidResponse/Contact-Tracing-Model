package uk.co.ramp.io.readers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import uk.co.ramp.io.InfectionRates;
import uk.co.ramp.people.VirusStatus;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static uk.co.ramp.people.VirusStatus.*;

public class InfectionRateReader {

    public InfectionRates read(Reader reader) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        Type empMapType = new TypeToken<Map<VirusStatus, Double>>() {}.getType();
        Map<VirusStatus, Double> type = gsonBuilder.setPrettyPrinting().create().fromJson(reader, empMapType);
        return new InfectionRates(type);
    }

    public void create(Writer writer) {
        Map<VirusStatus, Double> type = new HashMap<>();

        type.put(SEVERELY_SYMPTOMATIC,1.0);
        type.put(SYMPTOMATIC,0.9);
        type.put(ASYMPTOMATIC,0.5);
        type.put(PRESYMPTOMATIC,0.4);

        InfectionRates infectionRates = new InfectionRates(type);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(infectionRates, writer);

    }

}
