package uk.co.ramp.utilities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@TypeAdapters
@JsonSerialize
@JsonDeserialize
@Immutable
public interface MeanMax {
    int mean();

    int max();
}
