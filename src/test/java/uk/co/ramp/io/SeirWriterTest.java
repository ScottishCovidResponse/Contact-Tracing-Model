package uk.co.ramp.io;

import org.junit.Test;
import uk.co.ramp.record.ImmutableSeirRecord;
import uk.co.ramp.record.SeirRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class SeirWriterTest {

    private final List<SeirRecord> seirRecordList = List.of(
            ImmutableSeirRecord.builder()
                    .time(0)
                    .s(1)
                    .e(2)
                    .i(3)
                    .r(4)
                    .build(),
            ImmutableSeirRecord.builder()
                    .time(1)
                    .s(2)
                    .e(3)
                    .i(4)
                    .r(5)
                    .build()
    );

    @Test
    public void write() throws IOException {
        StringWriter stringWriter = new StringWriter();
        new SeirWriter().write(stringWriter, seirRecordList);

        String expectedCsvString = "\"time\",\"s\",\"e\",\"i\",\"r\"\n" +
                "0,1,2,3,4\n" +
                "1,2,3,4,5\n";
        assertThat(stringWriter.toString()).isEqualTo(expectedCsvString);
    }
}