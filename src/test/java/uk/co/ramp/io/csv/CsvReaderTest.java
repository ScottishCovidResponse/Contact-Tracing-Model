package uk.co.ramp.io.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.List;
import org.junit.Test;

public class CsvReaderTest {

  @Test
  public void testRead() throws IOException {
    var csvReader = new CsvReader();

    var csvString = "\"a\",\"b\",\"c\"\n" + "0,1,2\n" + "1,2,3\n";
    var stringReader = new StringReader(csvString);
    List<ImmutableExampleCsvPojo> csvPojo =
        csvReader.read(stringReader, ImmutableExampleCsvPojo.class);

    var record1 = ImmutableExampleCsvPojo.builder().a(0).b(1).c(2).build();
    var record2 = ImmutableExampleCsvPojo.builder().a(1).b(2).c(3).build();

    assertThat(csvPojo).containsExactly(record1, record2);
  }

  @Test
  public void testBrokenReader() throws IOException {
    var mockReader = mock(Reader.class);
    when(mockReader.read(any(char[].class), anyInt(), anyInt())).thenThrow(IOException.class);
    when(mockReader.read(any(char[].class))).thenThrow(IOException.class);
    when(mockReader.read()).thenThrow(IOException.class);
    when(mockReader.read(any(CharBuffer.class))).thenThrow(IOException.class);

    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> new CsvReader().read(mockReader, ImmutableExampleCsvPojo.class));
  }
}
