package uk.co.ramp.io.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvWriterTest {

    @Test
    public void testWrite() throws IOException {
        var record1 = ImmutableExampleCsvPojo.builder().a(0).b(1).c(2).build();
        var record2 = ImmutableExampleCsvPojo.builder().a(1).b(2).c(3).build();

        StringWriter stringWriter = new StringWriter();
        CsvWriter csvWriter = new CsvWriter();
        csvWriter.write(stringWriter, List.of(record1, record2), ImmutableExampleCsvPojo.class);

        var actualCsvString = stringWriter.toString();
        stringWriter.close();

        var expectedCsvString = "\"a\",\"b\",\"c\"\n" +
                "0,1,2\n" +
                "1,2,3\n";

        assertThat(actualCsvString).isEqualTo(expectedCsvString);
    }

    @Test
    public void testBrokenWriter() throws IOException {
        var record1 = ImmutableExampleCsvPojo.builder().a(0).b(1).c(2).build();

        Writer mockWriter = mock(Writer.class);
        when(mockWriter.append(anyChar())).thenThrow(IOException.class);
        when(mockWriter.append(any(CharSequence.class), anyInt(), anyInt())).thenThrow(IOException.class);
        doThrow(IOException.class).when(mockWriter).write(anyInt());
        doThrow(IOException.class).when(mockWriter).write(any(char[].class), anyInt(), anyInt());

        assertThatExceptionOfType(IOException.class).isThrownBy(() -> new CsvWriter().write(mockWriter, List.of(record1), ImmutableExampleCsvPojo.class));
    }
}