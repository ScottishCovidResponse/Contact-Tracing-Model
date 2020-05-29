package uk.co.ramp.io.readers;

import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.io.StandardProperties;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardPropertiesReaderTest {
    private static final String mockStandardProperties = "{" +
            "  'populationSize': 10000," +
            "  'timeLimit': 100," +
            "  'initialExposures': 1000," +
            "  'seed': 0," +
            "  'steadyState': true" +
            "}";

    private StandardPropertiesReader standardPropertiesReader;

    @Before
    public void setUp() {
        standardPropertiesReader = new StandardPropertiesReader();
    }

    @Test
    public void testRead() throws IOException {
        var underlyingReader = new BufferedReader(new StringReader(mockStandardProperties));
        StandardProperties standardProperties = standardPropertiesReader.read(underlyingReader);

        assertThat(standardProperties.initialExposures()).isEqualTo(1000);
        assertThat(standardProperties.populationSize()).isEqualTo(10000);
        assertThat(standardProperties.seed()).isZero();
        assertThat(standardProperties.timeLimit()).isEqualTo(100);
        assertThat(standardProperties.steadyState()).isTrue();
    }

    @Test
    public void testCreate() throws IOException {
        var stringWriter = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(stringWriter)) {
            standardPropertiesReader.create(bw);
        }

        var expectedStandardPropertiesJsonElement = JsonParser.parseString(mockStandardProperties);

        var actualStandardPropertiesString = stringWriter.toString();
        var actualStandardPropertiesJsonElement = JsonParser.parseString(actualStandardPropertiesString);

        assertThat(actualStandardPropertiesJsonElement).isEqualTo(expectedStandardPropertiesJsonElement);
    }
}