package uk.co.ramp.io.readers;

import com.google.gson.JsonParser;
import org.junit.Test;
import uk.co.ramp.io.types.DiseaseProperties;
import uk.co.ramp.io.types.ImmutableDiseaseProperties;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.io.ProgressionDistribution.FLAT;

public class DiseasePropertiesReaderTest {
    private static final String mockDiseaseSettings = "{ " +
            "   'meanTimeToInfectious': 3.0, " +
            "   'meanTimeToInfected': 3.0, " +
            "   'meanTimeToFinalState': 7.0, " +
            "   'maxTimeToInfectious': 14.0, " +
            "   'maxTimeToInfected': 14.0, " +
            "   'maxTimeToFinalState': 14.0, " +
            "   'exposureTuning': 160, " +
            "   'meanTestTime': 1, " +
            "   'maxTestTime': 3, " +
            "   'testAccuracy': 0.95, " +
            "   'exposureThreshold': 10, " +
            "   'randomInfectionRate': 0.01, " +
            "   'progressionDistribution':  'FLAT' }";

    @Test
    public void testRead() {
        var underlyingReader = new BufferedReader(new StringReader(mockDiseaseSettings));

        var reader = new DiseasePropertiesReader();
        DiseaseProperties actualDiseaseProperties = reader.read(underlyingReader);

        var expectedDiseaseProperties = ImmutableDiseaseProperties.builder()
                .exposureTuning(160)
                .meanTimeToInfectious(3.0)
                .meanTimeToInfected(3.0)
                .meanTimeToFinalState(7.0)
                .maxTimeToInfectious(14.0)
                .maxTimeToInfected(14.0)
                .maxTimeToFinalState(14.0)
                .randomInfectionRate(0.01)
                .meanTestTime(1)
                .maxTestTime(3)
                .testAccuracy(0.95)
                .exposureThreshold(10)
                .progressionDistribution(FLAT)
                .build();
        assertThat(actualDiseaseProperties).isEqualTo(expectedDiseaseProperties);
    }

    @Test
    public void testCreate() throws IOException {
        var stringWriter = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(stringWriter)) {
            new DiseasePropertiesReader().create(bw);
        }

        var expectedDiseaseSettingsJsonElement = JsonParser.parseString(mockDiseaseSettings);

        var actualDiseaseSettingsString = stringWriter.toString();
        var actualDiseaseSettingsJsonElement = JsonParser.parseString(actualDiseaseSettingsString);

        assertThat(actualDiseaseSettingsJsonElement).isEqualTo(expectedDiseaseSettingsJsonElement);
    }
}