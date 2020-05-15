package uk.co.ramp.io.csv;

import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CsvWriter {
    public <T> void write(Writer writer, List<T> records, Class<T> classType) throws IOException {
        CsvMapper csvMapper = new CsvMapper().configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        CsvSchema schema = csvMapper.typedSchemaFor(classType).withHeader();
        csvMapper.writerFor(classType).with(schema).writeValues(writer).writeAll(records);
    }
}
