package uk.co.ramp.people;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.ramp.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.core.StringContains.containsString;

public class CaseTest {

    private Case person;
    private final Random random = TestUtils.getRandom();


    @Before
    public void setup() {
        ImmutableHuman human = ImmutableHuman.builder().age(50).compliance(0.5).health(0.5).gender(Gender.FEMALE).id(1).build();
        person = new Case(human);
    }

    @Test
    public void validTransitions() {


        for (int i = 0; i < 100; i++) {
            while (person.status() != VirusStatus.DEAD && person.status() != VirusStatus.RECOVERED) {
                VirusStatus currentStatus = person.status();
                List<VirusStatus> validOptions = currentStatus.getValidTransitions();
                int size = validOptions.size();
                VirusStatus next = validOptions.get(random.nextInt(size));
                person.setStatus(next);

                Assert.assertEquals(next, person.status());
            }
        }
    }

    @Test
    public void invalidTransitions() {

        for (int i = 0; i < 10; i++) {

            VirusStatus currentStatus = person.status();
            List<VirusStatus> validOptions = currentStatus.getValidTransitions();
            List<VirusStatus> invalidOptions = new ArrayList<>(List.of(VirusStatus.values()));
            validOptions.forEach(invalidOptions::remove);
            int size = invalidOptions.size();
            VirusStatus next = invalidOptions.get(random.nextInt(size));

            try {
                person.setStatus(next);
                Assert.fail();
            } catch (InvalidStatusTransitionException e) {
                Assert.assertThat(e.getMessage(), containsString(next.toString()));
                Assert.assertThat(e.getMessage(), containsString(currentStatus.toString()));
            }
        }
    }


}