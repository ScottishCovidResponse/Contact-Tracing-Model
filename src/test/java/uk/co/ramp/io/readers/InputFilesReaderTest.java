package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import org.junit.Test;
import uk.co.ramp.io.types.ImmutableInputFiles;
import uk.co.ramp.io.types.InputFiles;

public class InputFilesReaderTest {

  private static final String mockInputLocations =
      "{\n"
          + "  \"runSettings\": \"runSettings.json\",\n"
          + "  \"contactData\": \"contactData.csv\",\n"
          + "  \"ageData\": \"ageData.csv\",\n"
          + "  \"initialExposures\": \"initialExposures.csv\",\n"
          + "  \"tracingPolicies\": \"tracingPolicies.json\",\n"
          + "  \"isolationPolicies\": \"isolationPolicies.json\",\n"
          + "  \"infectionRates\": \"infectionRates.json\",\n"
          + "  \"ageDependentHealth\": \"ageDependentHealth.json\"\n"
          + "}";

  @Test
  public void testRead() {
    var underlyingReader = new BufferedReader(new StringReader(mockInputLocations));

    var reader = new InputFilesReader();
    InputFiles actualInputLocations = reader.read(underlyingReader);

    var expectedInputLocation =
        ImmutableInputFiles.builder()
            .contactData("contactData.csv")
            .ageData("ageData.csv")
            .initialExposures("initialExposures.csv")
            .runSettings("runSettings.json")
            .tracingPolicies("tracingPolicies.json")
            .isolationPolicies("isolationPolicies.json")
            .infectionRates("infectionRates.json")
            .ageDependentHealth("ageDependentHealth.json")
            .build();
    assertThat(actualInputLocations).isEqualTo(expectedInputLocation);
  }
}
