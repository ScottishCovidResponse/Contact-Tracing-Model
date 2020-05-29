package uk.co.ramp.io;

import org.springframework.stereotype.Service;
import uk.co.ramp.io.csv.CsvWriter;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.ImmutableCmptRecord;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompartmentWriter {
    public void write(Writer writer, List<CmptRecord> records) throws IOException {
        List<ImmutableCmptRecord> wrappedImmutableRecords = records.stream()
                .map(r -> ImmutableCmptRecord.builder().from(r).build())
                .collect(Collectors.toList());
        new CsvWriter().write(writer, wrappedImmutableRecords, ImmutableCmptRecord.class);
    }
}
