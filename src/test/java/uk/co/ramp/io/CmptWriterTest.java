package uk.co.ramp.io;

import org.junit.Test;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.ImmutableCmptRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CmptWriterTest {

    private final List<CmptRecord> compartmentRecordList = List.of(
            ImmutableCmptRecord.builder()
                    .time(0)
                    .s(1)
                    .e(2)
                    .a(3)
                    .p(4)
                    .sym(5)
                    .sev(6)
                    .r(7)
                    .d(8)
                    .build(),
            ImmutableCmptRecord.builder()
                    .time(1)
                    .s(2)
                    .e(3)
                    .a(4)
                    .p(5)
                    .sym(6)
                    .sev(7)
                    .r(8)
                    .d(9)
                    .build()
    );

    @Test
    public void write() throws IOException {
        StringWriter stringWriter = new StringWriter();
        new CompartmentWriter().write(stringWriter, compartmentRecordList);

        String expectedCsvString = "\"time\",\"s\",\"e\",\"a\",\"p\",\"sym\",\"sev\",\"r\",\"d\"\n" +
                "0,1,2,3,4,5,6,7,8\n" +
                "1,2,3,4,5,6,7,8,9\n";
        assertThat(stringWriter.toString()).isEqualTo(expectedCsvString);
    }
}