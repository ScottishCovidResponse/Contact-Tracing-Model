package uk.co.ramp.io.readers;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.immutables.value.Value.Immutable;
import uk.co.ramp.io.csv.CsvReader;

public class AgeDataReader {
  @Immutable
  @JsonSerialize
  @JsonDeserialize
  @JsonPropertyOrder({"id", "age"})
  interface AgeData {
    int id();

    int age();
  }

  public Map<Integer, Integer> read(Reader reader) throws IOException {
    return new CsvReader()
        .read(reader, ImmutableAgeData.class).stream().collect(toMap(AgeData::id, AgeData::age));
  }
}
