package uk.co.ramp.io.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.types.ImmutableInputFiles;
import uk.co.ramp.io.types.InputFiles;

public class FullPathInputFilesReaderTest {
  private final String baseInputLocations =
      "{\n"
          + "  \"runSettings\": \"runSettings.json\",\n"
          + "  \"populationSettings\": \"populationSettings.json\",\n"
          + "  \"diseaseSettings\": \"diseaseSettings.json\",\n"
          + "  \"contactData\": \"contactData.csv\",\n"
          + "  \"ageData\": \"ageData.csv\",\n"
          + "  \"initialExposures\": \"initialExposures.csv\",\n"
          + "  \"tracingPolicies\": \"tracingPolicies.json\",\n"
          + "  \"isolationPolicies\": \"isolationPolicies.json\",\n"
          + "  \"infectionRates\": \"infectionRates.json\"\n"
          + "}";

  private final InputFiles baseInputFiles =
      ImmutableInputFiles.builder()
          .contactData("contactData.csv")
          .ageData("ageData.csv")
          .initialExposures("initialExposures.csv")
          .isolationPolicies("isolationPolicies.json")
          .runSettings("runSettings.json")
          .tracingPolicies("tracingPolicies.json")
          .infectionRates("infectionRates.json")
          .build();

  private final String overrideInputFolderLocation = "overrideFolder1";
  private final InputFilesReader inputFilesReader = mock(InputFilesReader.class);
  private final DirectoryList directoryList = mock(DirectoryList.class);
  private final Reader underLyingBaseInputFilesReader = mock(Reader.class);

  @Before
  public void setUp() {
    when(inputFilesReader.read(eq(underLyingBaseInputFilesReader))).thenReturn(baseInputFiles);
    when(directoryList.listFiles(eq(overrideInputFolderLocation)))
        .thenAnswer(a -> Stream.of("isolationPolicies.json", "tracingPolicies.json"));
  }

  @Test
  public void testRead() {
    var reader =
        new FullPathInputFilesReader(
            inputFilesReader, directoryList, overrideInputFolderLocation, "defaultInputFolder");

    var expectedInputFiles =
        ImmutableInputFiles.builder()
            .contactData("defaultInputFolder/contactData.csv")
            .ageData("defaultInputFolder/ageData.csv")
            .initialExposures("defaultInputFolder/initialExposures.csv")
            .isolationPolicies("overrideFolder1/isolationPolicies.json")
            .runSettings("defaultInputFolder/runSettings.json")
            .tracingPolicies("overrideFolder1/tracingPolicies.json")
            .infectionRates("defaultInputFolder/infectionRates.json")
            .build();

    assertThat(reader.read(underLyingBaseInputFilesReader)).isEqualTo(expectedInputFiles);
  }
}
