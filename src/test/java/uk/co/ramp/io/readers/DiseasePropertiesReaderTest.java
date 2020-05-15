package uk.co.ramp.io.readers;

import com.google.gson.JsonParser;
import org.junit.Test;
import uk.co.ramp.io.DiseaseProperties;
import uk.co.ramp.io.ImmutableDiseaseProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ramp.io.ProgressionDistribution.FLAT;

public class DiseasePropertiesReaderTest {
    private static final String mockDiseaseSettings = "{ " +
            "  'meanTimeToInfected': 3.0, " +
            "   'exposureTuning': 160, " +
            "   'meanTimeToRecovered': 7.0, " +
            "   'randomInfectionRate': 0.01, " +
            "   'progressionDistribution':  'FLAT' }";

    @Test
    public void testRead() {
        var underlyingReader = new BufferedReader(new StringReader(mockDiseaseSettings));

        var reader = new DiseasePropertiesReader();
        DiseaseProperties actualDiseaseProperties = reader.read(underlyingReader);

        var expectedDiseaseProperties = ImmutableDiseaseProperties.builder()
                .exposureTuning(160)
                .meanTimeToInfected(3.0)
                .meanTimeToRecovered(7.0)
                .randomInfectionRate(0.01)
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