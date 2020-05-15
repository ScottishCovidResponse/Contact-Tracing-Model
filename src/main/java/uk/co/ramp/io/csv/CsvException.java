package uk.co.ramp.io.csv;

public class CsvException extends RuntimeException {
    private static final String msg = "Error occurred when processing CSV. ";

    public CsvException(Exception e) {
        super(msg, e);
    }

    public CsvException(String s, Exception e) {
        super(msg + s, e);
    }

    public CsvException(String s) {
        super(s);
    }
}
