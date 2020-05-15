package uk.co.ramp.io.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.Reader;
import java.util.List;


public class CsvReader {
    public <T> List<T> read(Reader reader, Class<T> classType) throws IOException {
        CsvMapper csvMapper = new CsvMapper().configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
        CsvSchema schema = csvMapper.typedSchemaFor(classType).withHeader();
        MappingIterator<T> csvItr = csvMapper.readerFor(classType).with(schema).readValues(reader);
        return csvItr.readAll();
    }
}
