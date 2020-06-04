package uk.co.ramp;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.co.ramp.utilities.UtilitiesBean;

@TestConfiguration
public class TestConfig {

    @Bean
    public UtilitiesBean utilitiesBean() {
        return new UtilitiesBean();
    }

}
