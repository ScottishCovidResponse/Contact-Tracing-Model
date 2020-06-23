package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import java.io.*;
import org.junit.Test;
import uk.co.ramp.io.types.ImmutableInputFiles;
import uk.co.ramp.io.types.InputFiles;

public class InputFilesReaderTest {

  private static final String mockInputLocations =
      "{\n"
          + "  \"runSettings\": \"input/runSettings.json\",\n"
          + "  \"populationSettings\": \"input/populationSettings.json\",\n"
          + "  \"diseaseSettings\": \"input/diseaseSettings.json\",\n"
          + "  \"contactData\": \"input/contactData.csv\",\n"
          + "  \"initialExposures\": \"input/initialExposures.csv\"\n"
          + "}";

  @Test
  public void testRead() {
    var underlyingReader = new BufferedReader(new StringReader(mockInputLocations));

    var reader = new InputFilesReader();
    InputFiles actualInputLocations = reader.read(underlyingReader);

    var expectedInputLocation =
        ImmutableInputFiles.builder()
            .diseaseSettings("input/diseaseSettings.json")
            .contactData("input/contactData.csv")
            .initialExposures("input/initialExposures.csv")
            .populationSettings("input/populationSettings.json")
            .runSettings("input/runSettings.json")
            .build();
    assertThat(actualInputLocations).isEqualTo(expectedInputLocation);
  }

  @Test
  public void testCreate() throws IOException {
    var stringWriter = new StringWriter();
    try (BufferedWriter bw = new BufferedWriter(stringWriter)) {
      new InputFilesReader().create(bw);
    }

    var expectedInputFilesJsonElement = JsonParser.parseString(mockInputLocations);

    var actualInputFilesString = stringWriter.toString();
    var actualInputFilesJsonElement = JsonParser.parseString(actualInputFilesString);

    assertThat(actualInputFilesJsonElement).isEqualTo(expectedInputFilesJsonElement);
  }
}
