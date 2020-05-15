package uk.co.ramp.io;

import uk.co.ramp.io.csv.CsvWriter;
import uk.co.ramp.record.ImmutableSeirRecord;
import uk.co.ramp.record.SeirRecord;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class SeirWriter {
    public void write(Writer writer, List<SeirRecord> records) throws IOException {
        List<ImmutableSeirRecord> wrappedImmutableRecords = records.stream()
                .map(r -> ImmutableSeirRecord.builder().from(r).build())
                .collect(Collectors.toList());
        new CsvWriter().write(writer, wrappedImmutableRecords, ImmutableSeirRecord.class);
    }
}
