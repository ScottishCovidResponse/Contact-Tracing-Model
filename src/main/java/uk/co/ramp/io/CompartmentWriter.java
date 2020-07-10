package uk.co.ramp.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ramp.io.csv.CsvWriter;
import uk.co.ramp.io.types.CmptRecord;
import uk.co.ramp.io.types.ImmutableCmptRecord;

@Service
public class CompartmentWriter {
  public void write(Writer writer, List<CmptRecord> records) throws IOException {
    List<ImmutableCmptRecord> wrappedImmutableRecords =
        records.stream()
            .map(r -> ImmutableCmptRecord.builder().from(r).build())
            .sorted(Comparator.comparing(ImmutableCmptRecord::time, Comparator.naturalOrder()))
            .collect(Collectors.toList());
    new CsvWriter().write(writer, wrappedImmutableRecords, ImmutableCmptRecord.class);
  }
}
