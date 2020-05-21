package uk.co.ramp.utilities;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String s) {
        super(s);
    }
}
