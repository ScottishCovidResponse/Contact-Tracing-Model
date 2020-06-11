package uk.co.ramp.io.types;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@Immutable
public interface InputFiles {

    String runSettings();

    String populationSettings();

    String diseaseSettings();

    String contactData();

    String initialExposures();

}
