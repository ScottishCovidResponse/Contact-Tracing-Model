package uk.co.ramp.people;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.co.ramp.LogSpy;
import uk.co.ramp.TestUtils;
import uk.co.ramp.event.types.ContactEvent;
import uk.co.ramp.event.types.ImmutableContactEvent;

import java.util.*;

import static org.hamcrest.core.StringContains.containsString;
import static uk.co.ramp.people.AlertStatus.*;
import static uk.co.ramp.people.VirusStatus.*;

public class CaseTest {

    private Case person;
    @Rule
    public LogSpy logSpy = new LogSpy();
    private final Random random = TestUtils.getRandom();
    private ImmutableHuman human;

    @Before
    public void setup() {
        human = ImmutableHuman.builder().age(random.nextInt(100)).compliance(random.nextDouble()).health(random.nextDouble()).gender(Gender.FEMALE).id(1).build();
        person = new Case(human);
    }

    @Test
    public void validTransitions() {

        for (int i = 0; i < 100; i++) {
            while (person.status() != DEAD && person.status() != RECOVERED) {
                VirusStatus currentStatus = person.status();
                List<VirusStatus> validOptions = currentStatus.getValidTransitions();
                int size = validOptions.size();
                VirusStatus next = validOptions.get(random.nextInt(size));
                person.setVirusStatus(next);

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
                person.setVirusStatus(next);
                Assert.fail();
            } catch (InvalidStatusTransitionException e) {
                Assert.assertThat(e.getMessage(), containsString(next.toString()));
                Assert.assertThat(e.getMessage(), containsString(currentStatus.toString()));
            }
        }
    }


    @Test
    public void wasInfectious() {
        Assert.assertFalse(person.wasInfectiousWhenTested());
        person.setWasInfectiousWhenTested(true);
        Assert.assertTrue(person.wasInfectiousWhenTested());
    }

    @Test
    public void getDefault() {
        Assert.assertEquals(-1, Case.getDefault());
    }

    @Test
    public void getRandomInfection() {
        Assert.assertEquals(-2, Case.getRandomInfection());

    }

    @Test
    public void getInitial() {
        Assert.assertEquals(-3, Case.getInitial());
    }

    @Test
    public void exposedTime() {

        Assert.assertEquals(Case.getDefault(), person.exposedTime());
        int time = random.nextInt(100);
        person.setExposedTime(time);

        Assert.assertEquals(time, person.exposedTime());


    }


    @Test(expected = InvalidStatusTransitionException.class)
    public void nextAlertStatusChange() {


        Assert.assertEquals(NONE, person.alertStatus());
        Assert.assertEquals(Case.getDefault(), person.nextAlertStatusChange());

        int time = random.nextInt(10);
        person.setAlertStatus(ALERTED);
        person.setNextAlertStatusChange(time);

        Assert.assertEquals(ALERTED, person.alertStatus());
        Assert.assertEquals(time, person.nextAlertStatusChange());

        time += random.nextInt(10);
        person.setAlertStatus(REQUESTED_TEST);
        person.setNextAlertStatusChange(time);

        Assert.assertEquals(REQUESTED_TEST, person.alertStatus());
        Assert.assertEquals(time, person.nextAlertStatusChange());

        time += random.nextInt(10);
        person.setAlertStatus(AWAITING_RESULT);
        person.setNextAlertStatusChange(time);

        Assert.assertEquals(AWAITING_RESULT, person.alertStatus());
        Assert.assertEquals(time, person.nextAlertStatusChange());

        time += random.nextInt(10);
        person.setAlertStatus(TESTED_POSITIVE);
        person.setNextAlertStatusChange(time);

        Assert.assertEquals(TESTED_POSITIVE, person.alertStatus());
        Assert.assertEquals(time, person.nextAlertStatusChange());

        try {
            person.setAlertStatus(ALERTED);
        } catch (InvalidStatusTransitionException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("It is not valid to transition between statuses TESTED_POSITIVE -> ALERTED"));
            throw e;
        }

    }

    @Test
    public void contactRecords() {

        Map<Integer, ContactEvent> contacts = new HashMap<>();

        for (int i = 0; i < random.nextInt(100) + 20; i++) {
            int id = random.nextInt(100);
            int weight = random.nextInt(100);
            int time = random.nextInt(100);

            ContactEvent cr = ImmutableContactEvent.builder().from(id).to(person.id()).label("").weight(weight).time(time).build();
            if (!contacts.containsKey(id)) {
                contacts.put(id, cr);
                person.addContact(cr);
            }
        }

        Set<ContactEvent> personRecords = person.contactRecords();
        Assert.assertEquals(contacts.size(), personRecords.size());
        for (ContactEvent cr : personRecords) {
            int id = cr.from();
            ContactEvent saved = contacts.get(id);
            Assert.assertEquals(saved, cr);
        }


    }

    @Test
    public void humanTests() {
        Assert.assertEquals(human.compliance(), person.compliance(), 1e-6);
        Assert.assertEquals(human.health(), person.health(), 1e-6);
        Assert.assertEquals(human.gender(), person.gender());
        Assert.assertEquals(human, person.getHuman());
    }

    @Test
    public void exposedBy() {
        Assert.assertEquals(Case.getDefault(), person.exposedTime());
        int id = random.nextInt(100);
        person.setExposedBy(id);

        Assert.assertEquals(id, person.exposedBy());
    }

    @Test(expected = InvalidStatusTransitionException.class)
    public void nextVirusStatusChange() {

        Assert.assertEquals(SUSCEPTIBLE, person.status());
        Assert.assertEquals(Case.getDefault(), person.nextVirusStatusChange());

        int time = random.nextInt(10);
        person.setVirusStatus(EXPOSED);
        person.setNextVirusStatusChange(time);

        Assert.assertFalse(person.isInfectious());
        Assert.assertEquals(EXPOSED, person.status());
        Assert.assertEquals(time, person.nextVirusStatusChange());

        time += random.nextInt(10);
        person.setVirusStatus(EXPOSED_2);
        person.setNextVirusStatusChange(time);

        Assert.assertTrue(person.isInfectious());
        Assert.assertEquals(EXPOSED_2, person.status());
        Assert.assertEquals(time, person.nextVirusStatusChange());

        time += random.nextInt(10);
        person.setVirusStatus(INFECTED_SYMP);
        person.setNextVirusStatusChange(time);

        Assert.assertTrue(person.isInfectious());
        Assert.assertEquals(INFECTED_SYMP, person.status());
        Assert.assertEquals(time, person.nextVirusStatusChange());


        time += random.nextInt(10);
        person.setVirusStatus(RECOVERED);
        person.setNextVirusStatusChange(time);

        Assert.assertFalse(person.isInfectious());
        Assert.assertEquals(RECOVERED, person.status());
        Assert.assertEquals(time, person.nextVirusStatusChange());

        try {
            person.setVirusStatus(DEAD);
        } catch (InvalidStatusTransitionException e) {
            Assert.assertThat(logSpy.getOutput(), containsString("It is not valid to transition between statuses RECOVERED -> DEAD"));
            throw e;
        }

    }


    @Test
    public void isInfectious() {

        person.setVirusStatus(EXPOSED);
        Assert.assertFalse(person.isInfectious());
        person.setVirusStatus(EXPOSED_2);
        Assert.assertTrue(person.isInfectious());
        person.setVirusStatus(INFECTED);
        Assert.assertTrue(person.isInfectious());

    }


    @Test
    public void getSource() {

        int time = random.nextInt(100);
        person.setExposedTime(time);

        String result = person.getSource();

        Assert.assertThat(result, containsString("" + person.id()));
        Assert.assertThat(result, containsString("" + time));


    }
}