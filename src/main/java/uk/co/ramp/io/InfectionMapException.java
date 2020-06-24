package uk.co.ramp.io;

public class InfectionMapException extends RuntimeException {

  public InfectionMapException(String message) {
    super(message);
  }

  public InfectionMapException(String message, Exception e) {
    super(message, e);
  }
}
