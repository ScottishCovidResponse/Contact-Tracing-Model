package uk.co.ramp.io.csv;

public class CsvException extends RuntimeException {
  private static final String MSG = "Error occurred when processing CSV. ";

  public CsvException(Exception e) {
    super(MSG, e);
  }

  public CsvException(String s, Exception e) {
    super(MSG + s, e);
  }

  public CsvException(String s) {
    super(s);
  }
}
