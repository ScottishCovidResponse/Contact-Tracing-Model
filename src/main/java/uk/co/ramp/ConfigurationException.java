package uk.co.ramp;

public class ConfigurationException extends RuntimeException {


    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }
}
