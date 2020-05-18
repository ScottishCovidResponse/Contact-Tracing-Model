package uk.co.ramp.people;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PersonTest {

    private ModifiablePerson person;

    @Before
    public void setup() {
        person = ModifiablePerson.create().
                setAge(50).
                setCompliance(0.5).
                setExposedBy(-1).
                setGender(Gender.FEMALE).
                setHealth(0.5).
                setId(1).
                setStatus(VirusStatus.SUSCEPTIBLE);
    }


    @Test
    public void validTransitions() {
        Random r = new Random();

        for (int i = 0; i < 100; i++) {
            while (person.status() != VirusStatus.DEAD && person.status() != VirusStatus.RECOVERED) {
                VirusStatus currentStatus = person.status();
                List<VirusStatus> validOptions = currentStatus.getValidTransitions();
                int size = validOptions.size();
                VirusStatus next = validOptions.get(r.nextInt(size));
                person.setStatus(next);

                Assert.assertEquals(next, person.status());
            }
        }
    }

    @Test
    public void invalidTransitions() {
        Random r = new Random();
        VirusStatus currentStatus = person.status();
        List<VirusStatus> validOptions = currentStatus.getValidTransitions();
        List<VirusStatus> c = new ArrayList<>();
        List<VirusStatus> invalidOptions = new ArrayList<>(List.of(VirusStatus.values()));
        c.addAll(invalidOptions);

        validOptions.forEach(invalidOptions::remove);
//        invalidOptions.removeAll(validOptions);
        int size = invalidOptions.size();

        VirusStatus next = invalidOptions.get(r.nextInt(size));
        System.out.println(person.status() + "   " + next);
        person.setStatus(next);
//        person.transitionTo(next);
        System.out.println(person.status());
        Assert.assertEquals(next, person.status());
    }


}