package uk.co.ramp.io;

import org.junit.Test;
import uk.co.ramp.record.CmptRecord;
import uk.co.ramp.record.ImmutableCmptRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CmptWriterTest {

    private final List<CmptRecord> seirRecordList = List.of(
            ImmutableCmptRecord.builder()
                    .time(0)
                    .s(1)
                    .e1(2)
                    .e2(3)
                    .ia(4)
                    .is(5)
                    .r(6)
                    .d(7)
                    .build(),
            ImmutableCmptRecord.builder()
                    .time(1)
                    .s(2)
                    .e1(3)
                    .e2(4)
                    .ia(5)
                    .is(6)
                    .r(7)
                    .d(8)
                    .build()
    );

    @Test
    public void write() throws IOException {
        StringWriter stringWriter = new StringWriter();
        new SeirWriter().write(stringWriter, seirRecordList);

        String expectedCsvString = "\"time\",\"s\",\"e1\",\"e2\",\"ia\",\"is\",\"r\",\"d\"\n" +
                "0,1,2,3,4,5,6,7\n" +
                "1,2,3,4,5,6,7,8\n";
        assertThat(stringWriter.toString()).isEqualTo(expectedCsvString);
    }
}