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
          + "  \"runSettings\": \"runSettings.json\",\n"
          + "  \"populationSettings\": \"populationSettings.json\",\n"
          + "  \"diseaseSettings\": \"diseaseSettings.json\",\n"
          + "  \"contactData\": \"contactData.csv\",\n"
          + "  \"ageData\": \"ageData.csv\",\n"
          + "  \"initialExposures\": \"initialExposures.csv\",\n"
          + "  \"tracingPolicies\": \"tracingPolicies.json\",\n"
          + "  \"isolationPolicies\": \"isolationPolicies.json\"\n"
          + "}";

  @Test
  public void testRead() {
    var underlyingReader = new BufferedReader(new StringReader(mockInputLocations));

    var reader = new InputFilesReader();
    InputFiles actualInputLocations = reader.read(underlyingReader);

    var expectedInputLocation =
        ImmutableInputFiles.builder()
            .diseaseSettings("diseaseSettings.json")
            .contactData("contactData.csv")
            .ageData("ageData.csv")
            .initialExposures("initialExposures.csv")
            .populationSettings("populationSettings.json")
            .runSettings("runSettings.json")
            .tracingPolicies("tracingPolicies.json")
            .isolationPolicies("isolationPolicies.json")
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
